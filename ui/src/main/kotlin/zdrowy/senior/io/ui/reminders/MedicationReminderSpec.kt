package zdrowy.senior.io.ui.reminders

data class MedicationReminderSpec(
    val role: ReminderRole,
    val patientUid: String,
    val patientName: String,
    val medicationId: String,
    val medicationName: String,
    val dosage: String,
    val scheduleTime: String
) {
    fun key(): String = "${role.name}|$patientUid|$medicationId|$scheduleTime"
}
