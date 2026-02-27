package zdrowy.senior.io.ui.reminders

internal object VisitReminderTimeUtils {
    fun triggerAtMillis(spec: VisitReminderSpec): Long {
        val offsetMs = spec.reminderOffsetMinutes * 60_000L
        return spec.scheduledAtMs - offsetMs
    }
}
