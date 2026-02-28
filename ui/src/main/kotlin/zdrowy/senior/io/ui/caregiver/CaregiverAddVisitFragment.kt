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
import zdrowy.senior.io.ui.caregiver.model.CaregiverVisitDoctorUi
import zdrowy.senior.io.ui.databinding.FragmentCaregiverAddVisitBinding
import zdrowy.senior.io.ui.patient.PatientAddDoctorFragment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CaregiverAddVisitFragment : Fragment() {
    private var _binding: FragmentCaregiverAddVisitBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CaregiverAddVisitViewModel by viewModel()
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    private val reminderOffsets = listOf(10, 15, 30, 60)
    private val addDoctorOptionId = "__add_doctor__"

    private var dependentItems: List<CaregiverVisitDependentUi> = emptyList()
    private var selectedDependentUid: String? = null
    private var doctorItems: List<CaregiverVisitDoctorUi> = emptyList()
    private var selectedDoctorId: String? = null
    private var copiedDoctorName: String? = null
    private var pendingCreatedDoctorId: String? = null
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
        setupDoctorDropdown()
        initDefaultDateTime()
        applyCopyArgsIfPresent()
        observeCreatedDoctorResult()

        viewModel.start()
        viewModel.dependents.observe(viewLifecycleOwner) { items ->
            dependentItems = items
            renderDependents(items)
        }
        viewModel.doctors.observe(viewLifecycleOwner) { items ->
            doctorItems = items
            renderDoctors(items)
            applyPendingDoctorSelection()
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
            selectedDoctorId = null
            binding.caregiverAddVisitTitle.setText("", false)
            observeDoctorsForSelectedDependent()
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

        observeDoctorsForSelectedDependent()
    }

    private fun setupDoctorDropdown() {
        binding.caregiverAddVisitTitle.setOnItemClickListener { _, _, position, _ ->
            val options = doctorDropdownOptions()
            val selected = options.getOrNull(position) ?: return@setOnItemClickListener
            if (selected.id == addDoctorOptionId) {
                navigateToAddDoctor()
                return@setOnItemClickListener
            }
            selectedDoctorId = selected.id
            binding.caregiverAddVisitTitleInput.error = null
        }
        binding.caregiverAddVisitTitle.setOnClickListener {
            if (doctorItems.isEmpty()) {
                navigateToAddDoctor()
            } else {
                binding.caregiverAddVisitTitle.showDropDown()
            }
        }
        binding.caregiverAddVisitTitle.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) return@setOnFocusChangeListener
            if (doctorItems.isEmpty()) {
                navigateToAddDoctor()
            } else {
                binding.caregiverAddVisitTitle.showDropDown()
            }
        }
        binding.caregiverAddVisitTitleInput.setEndIconOnClickListener {
            if (doctorItems.isEmpty()) {
                navigateToAddDoctor()
            } else {
                binding.caregiverAddVisitTitle.showDropDown()
            }
        }
    }

    private fun renderDoctors(items: List<CaregiverVisitDoctorUi>) {
        val options = doctorDropdownOptions(items)
        val labels = options.map { option ->
            if (option.id == addDoctorOptionId) {
                getString(R.string.caregiver_visit_add_doctor_action)
            } else {
                doctorLabel(option)
            }
        }
        val adapter = ArrayAdapter(
            requireContext(),
            com.google.android.material.R.layout.mtrl_auto_complete_simple_item,
            labels
        )
        binding.caregiverAddVisitTitle.setAdapter(adapter)
    }

    private fun observeDoctorsForSelectedDependent() {
        val patientUid = selectedDependentUid.orEmpty()
        if (patientUid.isBlank()) {
            doctorItems = emptyList()
            renderDoctors(emptyList())
            return
        }
        viewModel.observeDoctorsForPatient(patientUid)
    }

    private fun navigateToAddDoctor() {
        val patientUid = selectedDependentUid.orEmpty()
        if (patientUid.isBlank()) {
            binding.caregiverAddVisitDependentInput.error = getString(R.string.common_field_required)
            return
        }
        viewModel.prepareAddDoctor(
            patientUid = patientUid,
            onReady = {
                requireParentFragment().findNavController().navigate(R.id.patientAddDoctorFragment)
            },
            onError = {
                Toast.makeText(requireContext(), R.string.caregiver_visit_save_error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun observeCreatedDoctorResult() {
        val savedStateHandle = findNavController().currentBackStackEntry?.savedStateHandle ?: return
        savedStateHandle
            .getLiveData<String>(PatientAddDoctorFragment.RESULT_CREATED_DOCTOR_ID)
            .observe(viewLifecycleOwner) { doctorId ->
                if (doctorId.isNullOrBlank()) return@observe
                pendingCreatedDoctorId = doctorId
                applyPendingDoctorSelection()
                savedStateHandle.remove<String>(PatientAddDoctorFragment.RESULT_CREATED_DOCTOR_ID)
            }
    }

    private fun applyPendingDoctorSelection() {
        val pendingId = pendingCreatedDoctorId
        if (!pendingId.isNullOrBlank()) {
            val doctor = doctorItems.firstOrNull { it.id == pendingId }
            if (doctor != null) {
                selectedDoctorId = doctor.id
                binding.caregiverAddVisitTitle.setText(doctorLabel(doctor), false)
                binding.caregiverAddVisitTitleInput.error = null
                pendingCreatedDoctorId = null
                return
            }
        }
        val pendingName = copiedDoctorName
        if (!pendingName.isNullOrBlank()) {
            val doctor = doctorItems.firstOrNull { it.fullName.equals(pendingName, ignoreCase = true) }
            if (doctor != null) {
                selectedDoctorId = doctor.id
                binding.caregiverAddVisitTitle.setText(doctorLabel(doctor), false)
                binding.caregiverAddVisitTitleInput.error = null
                copiedDoctorName = null
            }
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
        val doctorId = selectedDoctorId
        val doctor = doctorItems.firstOrNull { it.id == doctorId }
        val doctorName = doctor?.fullName.orEmpty()
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
        if (doctorId.isNullOrBlank() || doctorName.isBlank()) {
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
                title = doctorName,
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

        copiedDoctorName = arguments?.getString(ARG_COPY_TITLE).orEmpty().ifBlank { null }
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

    private fun doctorDropdownOptions(
        doctors: List<CaregiverVisitDoctorUi> = doctorItems
    ): List<CaregiverVisitDoctorUi> {
        if (doctors.isEmpty()) return emptyList()
        return doctors + CaregiverVisitDoctorUi(
            id = addDoctorOptionId,
            fullName = getString(R.string.caregiver_visit_add_doctor_action),
            specialization = "",
            phone = ""
        )
    }

    private fun doctorLabel(item: CaregiverVisitDoctorUi): String {
        val specialization = item.specialization.trim()
        return if (specialization.isBlank()) {
            item.fullName
        } else {
            "${item.fullName} (${specialization})"
        }
    }
}
