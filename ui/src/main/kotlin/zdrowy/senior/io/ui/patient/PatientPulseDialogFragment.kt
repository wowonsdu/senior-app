package zdrowy.senior.io.ui.patient

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientPulseBinding
import zdrowy.senior.io.domain.measurement.MeasurementSource
import zdrowy.senior.io.domain.measurement.MeasurementType
import java.util.Locale

class PatientPulseDialogFragment : DialogFragment() {
    private companion object {
        private const val DEFAULT_POSSIBLY_COMPLETE_SILENCE_MS = 1500
        private const val DEFAULT_COMPLETE_SILENCE_MS = 1000
        private const val DEFAULT_MINIMUM_LENGTH_MS = 1500
        private const val LISTEN_EXTENSION_MS = 5000
    }

    private var _binding: DialogPatientPulseBinding? = null
    private val binding get() = _binding!!
    private var speechRecognizer: SpeechRecognizer? = null
    private var recordingAnimator: ObjectAnimator? = null
    private var isEditMode: Boolean = false
    private val viewModel: PatientMeasurementDialogViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechToTextInternal()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientPulseBinding.inflate(LayoutInflater.from(context))
        val measurementId = arguments?.getString(ARG_MEASUREMENT_ID)
        val editMode = !measurementId.isNullOrBlank()
        isEditMode = editMode
        binding.pulseDialogClose.setOnClickListener { dismiss() }
        binding.pulseDialogCancel.setOnClickListener { dismiss() }
        binding.pulseDialogSave.setOnClickListener { saveMeasurement() }
        binding.pulseDialogVoice.voiceInputMic.setOnClickListener {
            startSpeechToText()
        }
        binding.pulseDialogVoice.root.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.pulseDialogDelete.visibility = if (editMode) View.VISIBLE else View.GONE
        if (editMode) {
            val value = arguments?.getFloat(ARG_MEASUREMENT_VALUE) ?: 0f
            binding.pulseDialogValue.setText(
                if (value == 0f) "" else value.toString()
            )
            binding.pulseDialogDelete.setOnClickListener {
                deleteMeasurement(measurementId.orEmpty())
            }
        }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        if (!isEditMode) {
            binding.root.post {
                startSpeechToText()
            }
        }
    }

    override fun onDestroyView() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        stopRecordingAnimation()
        _binding = null
        super.onDestroyView()
    }

    private fun startSpeechToText() {
        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
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
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    setRecordingActive(true)
                }
                override fun onBeginningOfSpeech() {
                    setRecordingActive(true)
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    Unit
                }
                override fun onError(error: Int) {
                    setRecordingActive(false)
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    setTextFromResults(partialResults)
                }
                override fun onResults(results: Bundle?) {
                    setRecordingActive(false)
                    setTextFromResults(results)
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
        speechRecognizer?.startListening(intent)
    }

    private fun setTextFromResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim().orEmpty()
        if (text.isNotBlank()) {
            val number = extractFirstNumber(text)
            binding.pulseDialogValue.setText(number)
        }
    }

    private fun extractFirstNumber(text: String): String {
        val match = Regex("\\d+(?:[\\.,]\\d+)?").find(text)
        return match?.value?.replace(',', '.') ?: text
    }

    private fun setRecordingActive(isActive: Boolean) {
        binding.pulseDialogVoice.voiceInputLabel.isVisible = !isActive
        binding.pulseDialogVoice.voiceInputBadge.isVisible = isActive
        binding.pulseDialogVoice.voiceInputProgress.isVisible = isActive
        if (isActive) {
            startRecordingAnimation()
        } else {
            stopRecordingAnimation()
        }
    }

    private fun startRecordingAnimation() {
        val dot = binding.pulseDialogVoice.voiceInputBadgeDot
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
        binding.pulseDialogVoice.voiceInputBadgeDot.alpha = 1f
    }

    private fun saveMeasurement() {
        val measurementId = arguments?.getString(ARG_MEASUREMENT_ID)
        val editMode = !measurementId.isNullOrBlank()
        val value = binding.pulseDialogValue.text?.toString()
            ?.replace(',', '.')
            ?.toDoubleOrNull()
            ?: run {
                binding.pulseDialogValueInput.error = "Wprowadz wartosc"
                return
            }
        binding.pulseDialogValueInput.error = null
        val timestamp = if (editMode) {
            val argTimestamp = arguments?.getLong(ARG_MEASUREMENT_TIMESTAMP) ?: 0L
            if (argTimestamp == 0L) System.currentTimeMillis() else argTimestamp
        } else {
            System.currentTimeMillis()
        }
        if (editMode) {
            viewModel.updateSimpleMeasurement(
                id = measurementId.orEmpty(),
                type = MeasurementType.PULSE,
                value = value,
                timestamp = timestamp,
                onDone = { dismiss() }
            )
        } else {
            viewModel.addSimpleMeasurement(
                type = MeasurementType.PULSE,
                value = value,
                timestamp = timestamp,
                source = MeasurementSource.MANUAL,
                onDone = { dismiss() }
            )
        }
    }

    private fun deleteMeasurement(id: String) {
        viewModel.deleteMeasurement(id) { dismiss() }
    }
}
