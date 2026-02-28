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
import org.koin.androidx.viewmodel.ext.android.viewModel
import zdrowy.senior.io.domain.visit.VisitDraft
import zdrowy.senior.io.ui.R
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDependentUi
import zdrowy.senior.io.ui.databinding.FragmentCaregiverAddVisitBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CaregiverAddVisitFragment : Fragment() {
    private var _binding: FragmentCaregiverAddVisitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverAddVisitViewModel by viewModel()
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val reminderOffsets = listOf(10, 15, 30, 60)

    private var dependentItems: List<CaregiverVisitDependentUi> = emptyList()
    private var selectedDependentUid: String? = null
    private var selectedDateTimeMs: Long? = null
    private var selectedReminderOffsetMinutes: Int? = 15
    private var isCopyMode: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCaregiverAddVisitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.caregiverAddVisitToolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverAddVisitCancel.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.caregiverAddVisitSave.setOnClickListener {
            saveVisit()
        }
        binding.caregiverAddVisitDatetime.setOnClickListener {
            showDateTimePicker()
        }
        binding.caregiverAddVisitDatetimeInput.setEndIconOnClickListener {
            showDateTimePicker()
        }
        binding.caregiverAddVisitReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            binding.caregiverAddVisitReminderOffsetInput.visibility =
                if (isChecked) View.VISIBLE else View.GONE
            if (!isChecked) {
                binding.caregiverAddVisitReminderOffsetInput.error = null
            }
        }

        setupReminderOffsetDropdown()
        setupDependentDropdown()
        initDefaultDateTime()
        applyCopyArgsIfPresent()

        viewModel.start()
        viewModel.dependents.observe(viewLifecycleOwner) { items ->
            dependentItems = items
            renderDependents(items)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupDependentDropdown() {
        binding.caregiverAddVisitDependent.setOnItemClickListener { _, _, position, _ ->
            selectedDependentUid = dependentItems.getOrNull(position)?.uid
            binding.caregiverAddVisitDependentInput.error = null
        }
        binding.caregiverAddVisitDependent.setOnClickListener {
            binding.caregiverAddVisitDependent.showDropDown()
        }
        binding.caregiverAddVisitDependent.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.caregiverAddVisitDependent.showDropDown()
        }
    }

    private fun renderDependents(items: List<CaregiverVisitDependentUi>) {
        val labels = items.map { dependentLabel(it) }
        val adapter = ArrayAdapter(
            requireContext(),
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            labels
        )
        binding.caregiverAddVisitDependent.setAdapter(adapter)

        if (selectedDependentUid == null && items.isNotEmpty()) {
            selectedDependentUid = items.first().uid
        }

        val selectedIndex = items.indexOfFirst { it.uid == selectedDependentUid }
        if (selectedIndex >= 0) {
            binding.caregiverAddVisitDependent.setText(labels[selectedIndex], false)
        } else {
            binding.caregiverAddVisitDependent.setText("", false)
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
        binding.caregiverAddVisitReminderOffset.setAdapter(adapter)
        binding.caregiverAddVisitReminderOffset.setOnItemClickListener { _, _, position, _ ->
            selectedReminderOffsetMinutes = reminderOffsets.getOrNull(position)
            binding.caregiverAddVisitReminderOffsetInput.error = null
        }
        binding.caregiverAddVisitReminderOffset.setOnClickListener {
            binding.caregiverAddVisitReminderOffset.showDropDown()
        }
        binding.caregiverAddVisitReminderOffset.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.caregiverAddVisitReminderOffset.showDropDown()
        }
        selectedReminderOffsetMinutes = reminderOffsets.firstOrNull()
        if (labels.isNotEmpty()) {
            binding.caregiverAddVisitReminderOffset.setText(labels.first(), false)
        }
    }

    private fun initDefaultDateTime() {
        val calendar = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        selectedDateTimeMs = calendar.timeInMillis
        renderDateTime()
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
                        binding.caregiverAddVisitDatetimeInput.error = null
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
        binding.caregiverAddVisitDatetime.setText(
            if (dateTime == null) "" else dateTimeFormat.format(dateTime)
        )
    }

    private fun saveVisit() {
        val patientUid = selectedDependentUid
        val title = binding.caregiverAddVisitTitle.text?.toString()?.trim().orEmpty()
        val rawScheduledAtMs = selectedDateTimeMs
        val location = binding.caregiverAddVisitLocation.text?.toString()?.trim().orEmpty()
        val notes = binding.caregiverAddVisitNotes.text?.toString()?.trim().orEmpty()
        val reminderEnabled = binding.caregiverAddVisitReminderSwitch.isChecked
        val reminderOffset = if (reminderEnabled) selectedReminderOffsetMinutes else null
        val scheduledAtMs = rawScheduledAtMs?.let {
            if (isCopyMode) normalizeCopyDateTimeToUpcoming(it) else it
        }

        var valid = true
        if (patientUid.isNullOrBlank()) {
            binding.caregiverAddVisitDependentInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverAddVisitDependentInput.error = null
        }
        if (title.isBlank()) {
            binding.caregiverAddVisitTitleInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverAddVisitTitleInput.error = null
        }
        if (scheduledAtMs == null) {
            binding.caregiverAddVisitDatetimeInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverAddVisitDatetimeInput.error = null
        }
        if (reminderEnabled && reminderOffset == null) {
            binding.caregiverAddVisitReminderOffsetInput.error = getString(R.string.common_field_required)
            valid = false
        } else {
            binding.caregiverAddVisitReminderOffsetInput.error = null
        }
        if (!valid) return

        viewModel.addVisit(
            draft = VisitDraft(
                patientUid = patientUid!!,
                title = title,
                scheduledAtMs = scheduledAtMs!!,
                location = location,
                notes = notes,
                reminderEnabled = reminderEnabled,
                reminderOffsetMinutes = reminderOffset
            ),
            onDone = {
                findNavController().popBackStack()
            },
            onError = {
                Toast.makeText(requireContext(), R.string.caregiver_visit_save_error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun dependentLabel(item: CaregiverVisitDependentUi): String {
        return if (item.phone.isBlank()) {
            item.fullName
        } else {
            "${item.fullName} (${item.phone})"
        }
    }

    private fun applyCopyArgsIfPresent() {
        val copyVisitId = arguments?.getString(ARG_COPY_VISIT_ID).orEmpty()
        if (copyVisitId.isBlank()) return

        isCopyMode = true
        selectedDependentUid = arguments?.getString(ARG_COPY_PATIENT_UID)

        binding.caregiverAddVisitTitle.setText(arguments?.getString(ARG_COPY_TITLE).orEmpty())
        binding.caregiverAddVisitLocation.setText(arguments?.getString(ARG_COPY_LOCATION).orEmpty())
        binding.caregiverAddVisitNotes.setText(arguments?.getString(ARG_COPY_NOTES).orEmpty())

        val copiedScheduledAtMs = arguments?.getLong(ARG_COPY_SCHEDULED_AT_MS, 0L) ?: 0L
        if (copiedScheduledAtMs > 0L) {
            selectedDateTimeMs = copiedScheduledAtMs
            renderDateTime()
        }

        val reminderEnabled = arguments?.getBoolean(ARG_COPY_REMINDER_ENABLED, false) == true
        binding.caregiverAddVisitReminderSwitch.isChecked = reminderEnabled
        if (reminderEnabled) {
            val copiedOffset = if (arguments?.containsKey(ARG_COPY_REMINDER_OFFSET_MINUTES) == true) {
                arguments?.getInt(ARG_COPY_REMINDER_OFFSET_MINUTES)
            } else {
                null
            }
            selectedReminderOffsetMinutes = copiedOffset ?: reminderOffsets.firstOrNull()
            selectedReminderOffsetMinutes?.let { offset ->
                binding.caregiverAddVisitReminderOffset.setText(
                    getString(R.string.caregiver_visit_reminder_offset_option, offset),
                    false
                )
            }
            binding.caregiverAddVisitReminderOffsetInput.visibility = View.VISIBLE
        } else {
            selectedReminderOffsetMinutes = null
            binding.caregiverAddVisitReminderOffset.setText("", false)
            binding.caregiverAddVisitReminderOffsetInput.visibility = View.GONE
        }
    }

    private fun normalizeCopyDateTimeToUpcoming(baseMs: Long): Long {
        val now = System.currentTimeMillis()
        if (baseMs > now) return baseMs
        val calendar = Calendar.getInstance().apply { timeInMillis = baseMs }
        while (calendar.timeInMillis <= now) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return calendar.timeInMillis
    }

    companion object {
        const val ARG_COPY_VISIT_ID = "copyVisitId"
        const val ARG_COPY_PATIENT_UID = "copyPatientUid"
        const val ARG_COPY_TITLE = "copyTitle"
        const val ARG_COPY_SCHEDULED_AT_MS = "copyScheduledAtMs"
        const val ARG_COPY_LOCATION = "copyLocation"
        const val ARG_COPY_NOTES = "copyNotes"
        const val ARG_COPY_REMINDER_ENABLED = "copyReminderEnabled"
        const val ARG_COPY_REMINDER_OFFSET_MINUTES = "copyReminderOffsetMinutes"
    }
}
