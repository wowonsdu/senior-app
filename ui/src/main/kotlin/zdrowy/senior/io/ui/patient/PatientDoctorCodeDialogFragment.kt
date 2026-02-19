package zdrowy.senior.io.ui.patient

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.databinding.DialogPatientDoctorCodeBinding

class PatientDoctorCodeDialogFragment : DialogFragment() {
    private var _binding: DialogPatientDoctorCodeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogPatientDoctorCodeBinding.inflate(layoutInflater)
        binding.patientDoctorCodeClose.setOnClickListener { dismiss() }
        binding.patientDoctorCodeCopy.setOnClickListener { dismiss() }
        binding.patientDoctorCodeSms.setOnClickListener { dismiss() }

        return MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setView(binding.root)
            .create()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
