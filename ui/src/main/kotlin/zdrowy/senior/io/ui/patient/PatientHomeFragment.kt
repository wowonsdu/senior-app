package zdrowy.senior.io.ui.patient

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.FragmentPatientHomeBinding
import zdrowy.senior.io.domain.measurement.MeasurementType
import zdrowy.senior.io.domain.user.ClearActivePatientUseCase
import zdrowy.senior.io.domain.user.ClearCurrentUserRoleUseCase
import org.koin.android.ext.android.inject
import java.util.Locale

class PatientHomeFragment : Fragment() {
    private companion object {
        private const val DEFAULT_POSSIBLY_COMPLETE_SILENCE_MS = 1500
        private const val DEFAULT_COMPLETE_SILENCE_MS = 1000
        private const val DEFAULT_MINIMUM_LENGTH_MS = 1500
        private const val LISTEN_EXTENSION_MS = 5000
    }

    private var _binding: FragmentPatientHomeBinding? = null
    private val binding get() = _binding!!
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val viewModel: PatientHomeViewModel by viewModel()
    private val clearActivePatient: ClearActivePatientUseCase by inject()
    private val clearCurrentUserRole: ClearCurrentUserRoleUseCase by inject()
    private val disposables = CompositeDisposable()
    private var speechRecognizer: SpeechRecognizer? = null
    private var recordingAnimator: ObjectAnimator? = null
    private var lastTranscript: String = ""

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechToTextInternal()
        } else {
            showToast(getString(R.string.patient_home_voice_permission_denied))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientHomeBinding.inflate(inflater, container, false)
        binding.patientHomeToolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.patient_home_logout) {
                auth.signOut()
                disposables.add(
                    clearActivePatient()
                        .andThen(clearCurrentUserRole())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({
                            findNavController().navigate(
                                R.id.startupGateFragment,
                                null,
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.patientHomeFragment, true)
                                    .setLaunchSingleTop(true)
                                    .build()
                            )
                        }, {
                            findNavController().navigate(
                                R.id.startupGateFragment,
                                null,
                                NavOptions.Builder()
                                    .setPopUpTo(R.id.patientHomeFragment, true)
                                    .setLaunchSingleTop(true)
                                    .build()
                            )
                        })
                )
                true
            } else {
                false
            }
        }
        binding.patientHomeTileSugar.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientSugarDialog)
        }
        binding.patientHomeTileInsulin.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientInsulinDialog)
        }
        binding.patientHomeTilePressure.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientPressureDialog)
        }
        binding.patientHomeTilePulse.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientPulseDialog)
        }
        binding.patientHomeTileHistory.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientHistory)
        }
        binding.patientHomeTileSettings.setOnClickListener {
            findNavController().navigate(R.id.action_patientHome_to_patientSettings)
        }
        binding.patientHomeVoiceCard.setOnClickListener {
            startSpeechToText()
        }
        binding.patientHomeProfile.setOnClickListener {
            viewModel.onHeaderClicked()
        }
        binding.patientHomeProfileActionContainer.setOnClickListener {
            viewModel.onHeaderClicked()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.start()
        viewModel.headerState.observe(viewLifecycleOwner) { state ->
            binding.patientHomeProfileLabel.setText(state.labelRes)
            binding.patientHomeProfileName.text = state.fullName
            binding.patientHomeProfilePhone.text = state.phone
            binding.patientHomeAvatar.text = state.avatar
            binding.patientHomeProfileActionContainer.isVisible =
                state.action != PatientHomeHeaderAction.NONE
            binding.patientHomeProfileAction.setImageResource(state.actionIconRes)
        }
        viewModel.navTarget.observe(viewLifecycleOwner) { target ->
            if (target == PatientHomeNavTarget.CAREGIVER_LINK) {
                findNavController().navigate(R.id.action_patientHome_to_caregiverLink)
            }
            if (target == PatientHomeNavTarget.CAREGIVER_HOME) {
                findNavController().popBackStack()
            }
            if (target != null) viewModel.onNavigationHandled()
        }
        viewModel.voiceSummary.observe(viewLifecycleOwner) { summary ->
            if (summary == null) return@observe
            showVoiceSummary(summary)
            viewModel.onVoiceSummaryHandled()
        }
    }

    override fun onDestroyView() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        stopRecordingAnimation()
        disposables.clear()
        _binding = null
        super.onDestroyView()
    }

    private fun startSpeechToText() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            showToast(getString(R.string.patient_home_voice_not_available))
            return
        }
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechToTextInternal()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun startSpeechToTextInternal() {
        lastTranscript = ""
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    setRecordingActive(true)
                    showListeningPlaceholder()
                }
                override fun onBeginningOfSpeech() {
                    setRecordingActive(true)
                    showListeningPlaceholder()
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    setRecordingActive(false)
                }
                override fun onError(error: Int) {
                    setRecordingActive(false)
                    showToast(getString(R.string.patient_home_voice_error))
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    handleSpeechResults(partialResults, isFinal = false)
                }
                override fun onResults(results: Bundle?) {
                    setRecordingActive(false)
                    handleSpeechResults(results, isFinal = true)
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
                DEFAULT_POSSIBLY_COMPLETE_SILENCE_MS + LISTEN_EXTENSION_MS
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                DEFAULT_COMPLETE_SILENCE_MS + LISTEN_EXTENSION_MS
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                DEFAULT_MINIMUM_LENGTH_MS + LISTEN_EXTENSION_MS
            )
        }
        setRecordingActive(true)
        showListeningPlaceholder()
        speechRecognizer?.stopListening()
        speechRecognizer?.startListening(intent)
    }

    private fun handleSpeechResults(results: Bundle?, isFinal: Boolean) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim().orEmpty()
        if (text.isBlank()) {
            if (!isFinal) return
            val fallback = lastTranscript.trim()
            if (fallback.isNotBlank()) {
                viewModel.onVoiceText(fallback)
                return
            }
            showToast(getString(R.string.patient_home_voice_empty))
            clearTranscript()
            return
        }
        updateTranscript(text)
        if (!isFinal) return
        viewModel.onVoiceText(text)
    }

    private fun showVoiceSummary(summary: VoiceSaveSummary) {
        val saved = summary.savedTypes
            .map { typeLabel(it) }
            .filter { it.isNotBlank() }
        val skipped = summary.skippedTypes
            .map { typeLabel(it) }
            .filter { it.isNotBlank() }

        if (saved.isEmpty() && skipped.isEmpty()) {
            showToast(getString(R.string.patient_home_voice_empty))
            return
        }

        val parts = mutableListOf<String>()
        if (saved.isNotEmpty()) {
            parts += getString(R.string.patient_home_voice_saved, saved.joinToString(", "))
        }
        if (skipped.isNotEmpty()) {
            parts += getString(R.string.patient_home_voice_skipped, skipped.joinToString(", "))
        }
        showToast(parts.joinToString(". "))
    }

    private fun typeLabel(type: MeasurementType): String {
        return when (type) {
            MeasurementType.SUGAR -> getString(R.string.patient_home_tile_sugar)
            MeasurementType.INSULIN -> getString(R.string.patient_home_tile_insulin)
            MeasurementType.PRESSURE -> getString(R.string.patient_home_tile_pressure)
            MeasurementType.PULSE -> getString(R.string.patient_home_tile_pulse)
        }
    }

    private fun setRecordingActive(isActive: Boolean) {
        binding.patientHomeVoiceLabel.isVisible = !isActive
        binding.patientHomeVoiceRecordingBadge.isVisible = isActive
        binding.patientHomeVoiceProgress.isVisible = isActive
        if (isActive) {
            startRecordingAnimation()
        } else {
            stopRecordingAnimation()
        }
    }

    private fun startRecordingAnimation() {
        val dot = binding.patientHomeVoiceRecordingDot
        if (recordingAnimator == null) {
            recordingAnimator = ObjectAnimator.ofFloat(dot, View.ALPHA, 1f, 0.2f).apply {
                duration = 600
                repeatMode = ObjectAnimator.REVERSE
                repeatCount = ObjectAnimator.INFINITE
                interpolator = LinearInterpolator()
            }
        }
        recordingAnimator?.start()
    }

    private fun stopRecordingAnimation() {
        recordingAnimator?.cancel()
        binding.patientHomeVoiceRecordingDot.alpha = 1f
    }

    private fun showListeningPlaceholder() {
        binding.patientHomeVoiceTranscriptCard.isVisible = true
        binding.patientHomeVoiceTranscriptText.text =
            getString(R.string.patient_home_voice_transcript_placeholder)
    }

    private fun updateTranscript(text: String) {
        lastTranscript = text
        binding.patientHomeVoiceTranscriptCard.isVisible = true
        binding.patientHomeVoiceTranscriptText.text = text
    }

    private fun clearTranscript() {
        lastTranscript = ""
        binding.patientHomeVoiceTranscriptCard.isVisible = false
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
