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
import timber.log.Timber
import java.util.Locale

class PatientHomeFragment : Fragment() {
    private companion object {
        private const val SPEECH_SILENCE_WINDOW_MS = 10_000
        private const val SPEECH_MINIMUM_LENGTH_MS = 1500
        private const val SPEECH_START_TIMEOUT_MS = 20_000
        private const val SPEECH_RESTART_DELAY_MS = 250L
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
    private var sessionStartAtMs: Long = 0L
    private var hasSpeechStarted: Boolean = false
    private var listenAttemptToken: Int = 0

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
        listenAttemptToken++
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

    private fun startSpeechToTextInternal(isRestart: Boolean = false, delayMs: Long = 0L) {
        if (!isRestart) {
            lastTranscript = ""
            sessionStartAtMs = System.currentTimeMillis()
            hasSpeechStarted = false
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    setRecordingActive(true)
                    showListeningPlaceholder()
                }
                override fun onBeginningOfSpeech() {
                    hasSpeechStarted = true
                    setRecordingActive(true)
                    showListeningPlaceholder()
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    Unit
                }
                override fun onError(error: Int) {
                    if (shouldRetryNoSpeech(error)) {
                        startSpeechToTextInternal(isRestart = true, delayMs = SPEECH_RESTART_DELAY_MS)
                        return
                    }
                    setRecordingActive(false)
                    Timber.w("SpeechRecognizer error=%d", error)
                    if (!hasSpeechStarted && isNoSpeechError(error)) {
                        showNoSpeechHint()
                        return
                    }
                    showToast(getString(R.string.patient_home_voice_error))
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    handleSpeechResults(partialResults, isFinal = false)
                }
                override fun onResults(results: Bundle?) {
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
                SPEECH_SILENCE_WINDOW_MS
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                SPEECH_SILENCE_WINDOW_MS
            )
            putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,
                SPEECH_MINIMUM_LENGTH_MS
            )
        }
        setRecordingActive(true)
        showListeningPlaceholder()
        speechRecognizer?.cancel()
        val token = ++listenAttemptToken
        val start = Runnable {
            if (token != listenAttemptToken) return@Runnable
            speechRecognizer?.startListening(intent)
        }
        if (delayMs > 0) {
            binding.root.postDelayed(start, delayMs)
        } else {
            binding.root.post(start)
        }
    }

    private fun handleSpeechResults(results: Bundle?, isFinal: Boolean) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim().orEmpty()
        if (text.isBlank()) {
            if (!isFinal) return
            val fallback = lastTranscript.trim()
            if (fallback.isNotBlank()) {
                setRecordingActive(false)
                viewModel.onVoiceText(fallback)
                return
            }
            if (shouldRetryNoSpeech(null)) {
                startSpeechToTextInternal(isRestart = true, delayMs = SPEECH_RESTART_DELAY_MS)
                return
            }
            setRecordingActive(false)
            showNoSpeechHint()
            return
        }
        hasSpeechStarted = true
        updateTranscript(text)
        if (!isFinal) return
        setRecordingActive(false)
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

    private fun isNoSpeechError(error: Int): Boolean {
        return error == SpeechRecognizer.ERROR_CLIENT ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT ||
            error == SpeechRecognizer.ERROR_NO_MATCH
    }

    private fun shouldRetryNoSpeech(error: Int?): Boolean {
        if (hasSpeechStarted) return false
        if (error != null && !isNoSpeechError(error)) return false
        val elapsed = System.currentTimeMillis() - sessionStartAtMs
        return elapsed < SPEECH_START_TIMEOUT_MS
    }

    private fun showNoSpeechHint() {
        binding.patientHomeVoiceTranscriptCard.isVisible = true
        binding.patientHomeVoiceTranscriptText.text =
            getString(R.string.voice_input_no_speech_hint)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
