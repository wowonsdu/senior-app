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
import zdrowy.senior.io.ui.databinding.DialogPatientPressureBinding
import zdrowy.senior.io.domain.measurement.MeasurementSource
import java.util.Locale

class PatientPressureDialogFragment : DialogFragment() {
    private companion object {
        private const val SPEECH_SILENCE_WINDOW_MS = 10_000
        private const val SPEECH_MINIMUM_LENGTH_MS = 1500
        private const val SPEECH_START_TIMEOUT_MS = 20_000
        private const val SPEECH_RETRY_DELAY_INITIAL_MS = 1500L
        private const val SPEECH_RETRY_DELAY_STEP_MS = 1000L
        private const val SPEECH_RETRY_DELAY_MAX_MS = 5000L
    }

    private var _binding: DialogPatientPressureBinding? = null
    private val binding get() = _binding!!
    private var speechRecognizer: SpeechRecognizer? = null
    private var recordingAnimator: ObjectAnimator? = null
    private var isEditMode: Boolean = false
    private var sessionStartAtMs: Long = 0L
    private var hasSpeechStarted: Boolean = false
    private var listenAttemptToken: Int = 0
    private var retryDelayMs: Long = SPEECH_RETRY_DELAY_INITIAL_MS
    private val viewModel: PatientMeasurementDialogViewModel by viewModel()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechToTextInternal()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientPressureBinding.inflate(LayoutInflater.from(context))
        val measurementId = arguments?.getString(ARG_MEASUREMENT_ID)
        val editMode = !measurementId.isNullOrBlank()
        isEditMode = editMode
        binding.pressureDialogClose.setOnClickListener { dismiss() }
        binding.pressureDialogCancel.setOnClickListener { dismiss() }
        binding.pressureDialogSave.setOnClickListener { saveMeasurement() }
        binding.pressureDialogVoice.voiceInputMic.setOnClickListener {
            startSpeechToText()
        }
        binding.pressureDialogVoice.root.visibility = if (editMode) View.GONE else View.VISIBLE
        binding.pressureDialogDelete.visibility = if (editMode) View.VISIBLE else View.GONE
        if (editMode) {
            val systolic = arguments?.getInt(ARG_MEASUREMENT_SYSTOLIC) ?: 0
            val diastolic = arguments?.getInt(ARG_MEASUREMENT_DIASTOLIC) ?: 0
            if (systolic != 0) binding.pressureDialogSystolic.setText(systolic.toString())
            if (diastolic != 0) binding.pressureDialogDiastolic.setText(diastolic.toString())
            binding.pressureDialogDelete.setOnClickListener {
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
        listenAttemptToken++
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

    private fun startSpeechToTextInternal(isRestart: Boolean = false, delayMs: Long = 0L) {
        if (!isRestart) {
            sessionStartAtMs = System.currentTimeMillis()
            hasSpeechStarted = false
            retryDelayMs = SPEECH_RETRY_DELAY_INITIAL_MS
        }
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    setRecordingActive(true)
                }
                override fun onBeginningOfSpeech() {
                    hasSpeechStarted = true
                    retryDelayMs = SPEECH_RETRY_DELAY_INITIAL_MS
                    setRecordingActive(true)
                }
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() {
                    Unit
                }
                override fun onError(error: Int) {
                    if (shouldRetryNoSpeech(error)) {
                        scheduleRetry()
                        return
                    }
                    setRecordingActive(false)
                    if (!hasSpeechStarted && (isNoSpeechError(error) || error == SpeechRecognizer.ERROR_NO_MATCH)) {
                        showNoSpeechHint()
                    }
                }
                override fun onPartialResults(partialResults: Bundle?) {
                    setTextFromResults(partialResults)
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull()?.trim().orEmpty()
                    if (text.isBlank()) {
                        setRecordingActive(false)
                        showNoSpeechHint()
                        return
                    }
                    hasSpeechStarted = true
                    retryDelayMs = SPEECH_RETRY_DELAY_INITIAL_MS
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
        binding.pressureDialogVoice.voiceInputLabel.setText(R.string.patient_home_voice_title)
        setRecordingActive(true)
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

    private fun setTextFromResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim().orEmpty()
        if (text.isNotBlank()) {
            hasSpeechStarted = true
            val numbers = extractNumbers(text)
            if (numbers.isNotEmpty()) {
                binding.pressureDialogSystolic.setText(numbers[0])
            }
            if (numbers.size > 1) {
                binding.pressureDialogDiastolic.setText(numbers[1])
            }
        }
    }

    private fun extractNumbers(text: String): List<String> {
        return Regex("\\d+(?:[\\.,]\\d+)?").findAll(text)
            .map { it.value.replace(',', '.') }
            .toList()
    }

    private fun isNoSpeechError(error: Int): Boolean {
        return error == SpeechRecognizer.ERROR_CLIENT ||
            error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT
    }

    private fun shouldRetryNoSpeech(error: Int): Boolean {
        if (hasSpeechStarted) return false
        if (!isNoSpeechError(error)) return false
        val elapsed = System.currentTimeMillis() - sessionStartAtMs
        return elapsed < SPEECH_START_TIMEOUT_MS
    }

    private fun showNoSpeechHint() {
        binding.pressureDialogVoice.voiceInputLabel.text =
            getString(R.string.voice_input_no_speech_hint)
    }

    private fun scheduleRetry() {
        val delay = retryDelayMs
        retryDelayMs = (retryDelayMs + SPEECH_RETRY_DELAY_STEP_MS)
            .coerceAtMost(SPEECH_RETRY_DELAY_MAX_MS)
        startSpeechToTextInternal(isRestart = true, delayMs = delay)
    }

    private fun setRecordingActive(isActive: Boolean) {
        binding.pressureDialogVoice.voiceInputLabel.isVisible = !isActive
        binding.pressureDialogVoice.voiceInputBadge.isVisible = isActive
        binding.pressureDialogVoice.voiceInputProgress.isVisible = isActive
        if (isActive) {
            startRecordingAnimation()
        } else {
            stopRecordingAnimation()
        }
    }

    private fun startRecordingAnimation() {
        val dot = binding.pressureDialogVoice.voiceInputBadgeDot
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
        binding.pressureDialogVoice.voiceInputBadgeDot.alpha = 1f
    }

    private fun saveMeasurement() {
        val measurementId = arguments?.getString(ARG_MEASUREMENT_ID)
        val editMode = !measurementId.isNullOrBlank()
        val systolic = binding.pressureDialogSystolic.text?.toString()?.toIntOrNull()
        val diastolic = binding.pressureDialogDiastolic.text?.toString()?.toIntOrNull()
        if (systolic == null || diastolic == null) {
            binding.pressureDialogSystolicInput.error = if (systolic == null) "Wymagane" else null
            binding.pressureDialogDiastolicInput.error = if (diastolic == null) "Wymagane" else null
            return
        }
        binding.pressureDialogSystolicInput.error = null
        binding.pressureDialogDiastolicInput.error = null
        val timestamp = if (editMode) {
            val argTimestamp = arguments?.getLong(ARG_MEASUREMENT_TIMESTAMP) ?: 0L
            if (argTimestamp == 0L) System.currentTimeMillis() else argTimestamp
        } else {
            System.currentTimeMillis()
        }
        if (editMode) {
            viewModel.updatePressureMeasurement(
                id = measurementId.orEmpty(),
                systolic = systolic,
                diastolic = diastolic,
                timestamp = timestamp,
                onDone = { dismiss() }
            )
        } else {
            viewModel.addPressureMeasurement(
                systolic = systolic,
                diastolic = diastolic,
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
