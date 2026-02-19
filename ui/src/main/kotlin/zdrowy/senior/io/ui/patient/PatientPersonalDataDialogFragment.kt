package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientPersonalDataBinding

class PatientPersonalDataDialogFragment : DialogFragment() {
    private var _binding: DialogPatientPersonalDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientPersonalDataBinding.inflate(layoutInflater)
        binding.patientPersonalClose.setOnClickListener { dismiss() }
        binding.patientPersonalCancel.setOnClickListener { dismiss() }
        binding.patientPersonalSave.setOnClickListener { dismiss() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
