package zdrowy.senior.io.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VisitReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return

        val eventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty().ifBlank { return }
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()
        val visitTitle = intent.getStringExtra(EXTRA_VISIT_TITLE).orEmpty()
        val location = intent.getStringExtra(EXTRA_LOCATION).orEmpty()
        val scheduledAtMs = intent.getLongExtra(EXTRA_SCHEDULED_AT_MS, 0L)
        val reminderOffsetMinutes = intent.getIntExtra(EXTRA_REMINDER_OFFSET_MIN, 0)

        val notifier = VisitReminderNotifier(context.applicationContext)
        notifier.showReminder(
            eventId = eventId,
            patientName = patientName,
            visitTitle = visitTitle,
            location = location,
            scheduledAtMs = scheduledAtMs,
            reminderOffsetMinutes = reminderOffsetMinutes
        )
    }

    companion object {
        const val ACTION_REMINDER = "zdrowy.senior.io.VISIT_REMINDER"
        const val EXTRA_EVENT_ID = "extra_event_id"
        const val EXTRA_VISIT_ID = "extra_visit_id"
        const val EXTRA_PATIENT_UID = "extra_patient_uid"
        const val EXTRA_PATIENT_NAME = "extra_patient_name"
        const val EXTRA_VISIT_TITLE = "extra_visit_title"
        const val EXTRA_LOCATION = "extra_location"
        const val EXTRA_SCHEDULED_AT_MS = "extra_scheduled_at_ms"
        const val EXTRA_REMINDER_OFFSET_MIN = "extra_reminder_offset_min"
    }
}
