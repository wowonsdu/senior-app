package zdrowy.senior.io.ui.reminders

data class VisitReminderSpec(
    val visitId: String,
    val patientUid: String,
    val patientName: String,
    val visitTitle: String,
    val location: String,
    val scheduledAtMs: Long,
    val reminderOffsetMinutes: Int
) {
    fun key(): String {
        return "$visitId|$patientUid|$scheduledAtMs|$reminderOffsetMinutes"
    }

    fun eventId(): String {
        return "${visitId}_${scheduledAtMs}_${reminderOffsetMinutes}"
    }
}
