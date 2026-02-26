package zdrowy.senior.io.domain.settings.reminders

data class MedicationReminderEvent(
    val id: String,
    val patientUid: String,
    val medicationId: String,
    val scheduledAtMs: Long,
    val medicationName: String,
    val dosage: String,
    val scheduleTime: String
)
