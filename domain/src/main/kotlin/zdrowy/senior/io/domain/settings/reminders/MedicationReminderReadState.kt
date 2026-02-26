package zdrowy.senior.io.domain.settings.reminders

data class MedicationReminderReadState(
    val patientUid: String,
    val readEventIds: Set<String>
)
