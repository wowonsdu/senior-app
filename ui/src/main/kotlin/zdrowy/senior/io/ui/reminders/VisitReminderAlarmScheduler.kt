package zdrowy.senior.io.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

class VisitReminderAlarmScheduler(
    private val context: Context,
    private val cache: VisitReminderAlarmCache
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAll(specs: Collection<VisitReminderSpec>) {
        specs.forEach { schedule(it) }
        cache.save(specs)
    }

    fun cancel(spec: VisitReminderSpec) {
        val intent = buildIntent(spec)
        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(spec),
            intent,
            PendingIntent.FLAG_NO_CREATE or pendingIntentFlags()
        )
        if (pending != null) {
            alarmManager.cancel(pending)
            pending.cancel()
        }
    }

    fun cancelAll() {
        cache.load().forEach { cancel(it) }
        cache.clear()
    }

    fun rescheduleFromCache() {
        cache.load().forEach { schedule(it) }
    }

    fun schedule(spec: VisitReminderSpec) {
        val triggerAtMs = VisitReminderTimeUtils.triggerAtMillis(spec)
        if (triggerAtMs <= System.currentTimeMillis()) return

        val intent = buildIntent(spec)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_EVENT_ID, spec.eventId())
            .putExtra(VisitReminderAlarmReceiver.EXTRA_VISIT_ID, spec.visitId)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_PATIENT_UID, spec.patientUid)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_PATIENT_NAME, spec.patientName)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_VISIT_TITLE, spec.visitTitle)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_LOCATION, spec.location)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_SCHEDULED_AT_MS, spec.scheduledAtMs)
            .putExtra(VisitReminderAlarmReceiver.EXTRA_REMINDER_OFFSET_MIN, spec.reminderOffsetMinutes)

        val pending = PendingIntent.getBroadcast(
            context,
            requestCode(spec),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentFlags()
        )

        val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }

        if (canExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                pending
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMs,
                pending
            )
        }
    }

    private fun buildIntent(spec: VisitReminderSpec): Intent {
        val data = Uri.parse("senior://visit_reminder/${requestCode(spec)}")
        return Intent(context, VisitReminderAlarmReceiver::class.java)
            .setAction(VisitReminderAlarmReceiver.ACTION_REMINDER)
            .setData(data)
    }

    private fun requestCode(spec: VisitReminderSpec): Int {
        return spec.key().hashCode() and 0x7fffffff
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }
}
