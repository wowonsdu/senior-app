package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientAddMedBinding

class PatientAddMedDialogFragment : DialogFragment() {
    private var _binding: DialogPatientAddMedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientAddMedBinding.inflate(layoutInflater)
        binding.patientAddMedClose.setOnClickListener { dismiss() }
        binding.patientAddMedCancel.setOnClickListener { dismiss() }
        binding.patientAddMedSave.setOnClickListener { dismiss() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
