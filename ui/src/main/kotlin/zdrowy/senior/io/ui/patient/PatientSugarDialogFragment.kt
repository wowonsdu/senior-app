package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientSugarBinding

class PatientSugarDialogFragment : DialogFragment() {
    private var _binding: DialogPatientSugarBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientSugarBinding.inflate(layoutInflater)
        binding.sugarDialogClose.setOnClickListener {
            dismiss()
        }
        binding.sugarDialogCancel.setOnClickListener {
            dismiss()
        }
        binding.sugarDialogSave.setOnClickListener {
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
