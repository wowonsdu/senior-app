package zdrowy.senior.io.ui.patient

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientPressureBinding
import java.util.Locale

class PatientPressureDialogFragment : DialogFragment() {
    private var _binding: DialogPatientPressureBinding? = null
    private val binding get() = _binding!!
    private var speechRecognizer: SpeechRecognizer? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startSpeechToTextInternal()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientPressureBinding.inflate(LayoutInflater.from(context))
        binding.pressureDialogClose.setOnClickListener { dismiss() }
        binding.pressureDialogCancel.setOnClickListener { dismiss() }
        binding.pressureDialogSave.setOnClickListener { dismiss() }
        binding.pressureDialogMic.setOnClickListener {
            startSpeechToText()
        }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onStart() {
        super.onStart()
        binding.root.post {
            startSpeechToText()
        }
    }

    override fun onDestroyView() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
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
                override fun onReadyForSpeech(params: Bundle?) = Unit
                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onError(error: Int) = Unit
                override fun onPartialResults(partialResults: Bundle?) {
                    setTextFromResults(partialResults)
                }
                override fun onResults(results: Bundle?) {
                    setTextFromResults(results)
                }
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        speechRecognizer?.startListening(intent)
    }

    private fun setTextFromResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = matches?.firstOrNull()?.trim().orEmpty()
        if (text.isNotBlank()) {
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
}
