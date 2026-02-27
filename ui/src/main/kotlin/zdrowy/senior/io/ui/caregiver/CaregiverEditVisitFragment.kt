package zdrowy.senior.io.ui.caregiver

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.domain.visit.Visit
import zdrowy.senior.io.domain.visit.VisitUpdate
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDependentUi
import zdrowy.senior.io.ui.databinding.FragmentCaregiverEditVisitBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CaregiverEditVisitFragment : Fragment() {
    private var _binding: FragmentCaregiverEditVisitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverEditVisitViewModel by viewModel()
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val reminderOffsets = listOf(10, 15, 30, 60)

    private var dependentItems: List<CaregiverVisitDependentUi> = emptyList()
    private var selectedDependentUid: String? = null
    private var selectedDateTimeMs: Long? = null
    private var selectedReminderOffsetMinutes: Int? = null
    private var prefilled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverEditVisitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = visitId
        if (id.isNullOrBlank()) {
            findNavController().popBackStack()
            return
        }

        binding.caregiverEditVisitToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverEditVisitSave.setOnClickListener {
            saveVisit(id)
        }
        binding.caregiverEditVisitDelete.setOnClickListener {
            confirmDelete(id)
        }
        binding.caregiverEditVisitDatetime.setOnClickListener {
            showDateTimePicker()
        }
        binding.caregiverEditVisitDatetimeInput.setEndIconOnClickListener {
            showDateTimePicker()
        }
        binding.caregiverEditVisitReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.caregiverEditVisitReminderOffsetInput.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.caregiverEditVisitReminderOffsetInput.error = null
            }
        }

        setupReminderOffsetDropdown()
        setupDependentDropdown()

        viewModel.start(id)
        viewModel.dependents.observe(viewLifecycleOwner) { items ->
            dependentItems = items
            renderDependents(items)
        }
        viewModel.visit.observe(viewLifecycleOwner) { visit ->
            if (visit == null) {
                Toast.makeText(requireContext(), R.string.caregiver_visit_not_found, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@observe
            }
            if (!prefilled) {
                prefilled = true
                prefill(visit)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupDependentDropdown() {
        binding.caregiverEditVisitDependent.setOnItemClickListener { _, _, position, _ ->
            selectedDependentUid = dependentItems.getOrNull(position)?.uid
            binding.caregiverEditVisitDependentInput.error = null
        }
        binding.caregiverEditVisitDependent.setOnClickListener {
            binding.caregiverEditVisitDependent.showDropDown()
        }
        binding.caregiverEditVisitDependent.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.caregiverEditVisitDependent.showDropDown()
        }
    }

    private fun renderDependents(items: List<CaregiverVisitDependentUi>) {
        val labels = items.map { dependentLabel(it) }
        val adapter = ArrayAdapter(
            requireContext(),
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            labels
        )
        binding.caregiverEditVisitDependent.setAdapter(adapter)

        val selectedIndex = items.indexOfFirst { it.uid == selectedDependentUid }
        if (selectedIndex >= 0) {
            binding.caregiverEditVisitDependent.setText(labels[selectedIndex], false)
        }
    }

    private fun setupReminderOffsetDropdown() {
        val labels = reminderOffsets.map {
            getString(R.string.caregiver_visit_reminder_offset_option, it)
        }
        val adapter = ArrayAdapter(
            requireContext(),
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            labels
        )
        binding.caregiverEditVisitReminderOffset.setAdapter(adapter)
        binding.caregiverEditVisitReminderOffset.setOnItemClickListener { _, _, position, _ ->
            selectedReminderOffsetMinutes = reminderOffsets.getOrNull(position)
            binding.caregiverEditVisitReminderOffsetInput.error = null
        }
        binding.caregiverEditVisitReminderOffset.setOnClickListener {
            binding.caregiverEditVisitReminderOffset.showDropDown()
        }
        binding.caregiverEditVisitReminderOffset.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.caregiverEditVisitReminderOffset.showDropDown()
        }
    }

    private fun prefill(visit: Visit) {
        selectedDependentUid = visit.patientUid
        selectedDateTimeMs = visit.scheduledAtMs
        selectedReminderOffsetMinutes = visit.reminderOffsetMinutes

        binding.caregiverEditVisitTitle.setText(visit.title)
        binding.caregiverEditVisitLocation.setText(visit.location)
        binding.caregiverEditVisitNotes.setText(visit.notes)
        renderDateTime()

        val reminderEnabled = visit.reminderEnabled
        binding.caregiverEditVisitReminderSwitch.isChecked = reminderEnabled
        binding.caregiverEditVisitReminderOffsetInput.visibility =
            if (reminderEnabled) View.VISIBLE else View.GONE

        if (reminderEnabled) {
            val index = reminderOffsets.indexOf(visit.reminderOffsetMinutes)
                .takeIf { it >= 0 }
                ?: reminderOffsets.indexOf(15)
                    .takeIf { it >= 0 }
                    ?: 0
            selectedReminderOffsetMinutes = reminderOffsets.getOrNull(index)
            val label = getString(
                R.string.caregiver_visit_reminder_offset_option,
                selectedReminderOffsetMinutes ?: reminderOffsets.first()
            )
            binding.caregiverEditVisitReminderOffset.setText(label, false)
        }

        renderDependents(dependentItems)
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedDateTimeMs ?: System.currentTimeMillis()
        }
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        selectedDateTimeMs = calendar.timeInMillis
                        binding.caregiverEditVisitDatetimeInput.error = null
                        renderDateTime()
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun renderDateTime() {
        val dateTime = selectedDateTimeMs
        binding.caregiverEditVisitDatetime.setText(
            if (dateTime == null) "" else dateTimeFormat.format(dateTime)
        )
    }

    private fun saveVisit(visitId: String) {
        val patientUid = selectedDependentUid
        val title = binding.caregiverEditVisitTitle.text?.toString()?.trim().orEmpty()
        val scheduledAtMs = selectedDateTimeMs
        val location = binding.caregiverEditVisitLocation.text?.toString()?.trim().orEmpty()
        val notes = binding.caregiverEditVisitNotes.text?.toString()?.trim().orEmpty()
        val reminderEnabled = binding.caregiverEditVisitReminderSwitch.isChecked
        val reminderOffset = if (reminderEnabled) selectedReminderOffsetMinutes else null

        var valid = true
        if (patientUid.isNullOrBlank()) {
            binding.caregiverEditVisitDependentInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverEditVisitDependentInput.error = null
        }
        if (title.isBlank()) {
            binding.caregiverEditVisitTitleInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverEditVisitTitleInput.error = null
        }
        if (scheduledAtMs == null) {
            binding.caregiverEditVisitDatetimeInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverEditVisitDatetimeInput.error = null
        }
        if (reminderEnabled && reminderOffset == null) {
            binding.caregiverEditVisitReminderOffsetInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverEditVisitReminderOffsetInput.error = null
        }
        if (!valid) return

        viewModel.updateVisit(
            visitId = visitId,
            update = VisitUpdate(
                patientUid = patientUid,
                title = title,
                scheduledAtMs = scheduledAtMs,
                location = location,
                notes = notes,
                reminderEnabled = reminderEnabled,
                reminderOffsetMinutes = reminderOffset,
                isCompletedManual = null,
                completedAtMs = null
            ),
            onDone = {
                findNavController().popBackStack()
            },
            onError = {
                Toast.makeText(requireContext(), R.string.caregiver_visit_save_error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun confirmDelete(visitId: String) {
        MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Senior_Dialog)
            .setTitle(R.string.caregiver_visits_delete_title)
            .setMessage(R.string.caregiver_visits_delete_message)
            .setNegativeButton(R.string.caregiver_visit_form_cancel, null)
            .setPositiveButton(R.string.caregiver_visits_action_delete) { _, _ ->
                viewModel.removeVisit(
                    visitId = visitId,
                    onDone = {
                        findNavController().popBackStack()
                    },
                    onError = {
                        Toast.makeText(
                            requireContext(),
                            R.string.caregiver_visit_save_error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
            .show()
    }

    private fun dependentLabel(item: CaregiverVisitDependentUi): String {
        return if (item.phone.isBlank()) {
            item.fullName
        } else {
            "${item.fullName} (${item.phone})"
        }
    }

    private val visitId: String?
        get() = arguments?.getString("visitId")
}
