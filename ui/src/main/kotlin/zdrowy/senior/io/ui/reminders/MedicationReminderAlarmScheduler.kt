package zdrowy.senior.io.ui.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

class MedicationReminderAlarmScheduler(
    private val context: Context,
    private val cache: MedicationReminderAlarmCache
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAll(role: ReminderRole, specs: Collection<MedicationReminderSpec>) {
        specs.forEach { schedule(it) }
        cache.save(role, specs)
    }

    fun cancelAll(role: ReminderRole) {
        cache.load(role).forEach { cancel(it) }
        cache.clear(role)
    }

    fun rescheduleFromCache(role: ReminderRole) {
        cache.load(role).forEach { schedule(it) }
    }

    fun schedule(spec: MedicationReminderSpec) {
        val triggerAt = MedicationReminderTimeUtils.nextTriggerMillis(
            spec.scheduleTime,
            System.currentTimeMillis()
        ) ?: return
        scheduleAt(spec, triggerAt)
    }

    fun scheduleNextOccurrence(spec: MedicationReminderSpec) {
        schedule(spec)
    }

    fun cancel(spec: MedicationReminderSpec) {
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

    private fun scheduleAt(spec: MedicationReminderSpec, triggerAtMillis: Long) {
        val eventId = MedicationReminderTimeUtils.buildEventId(spec.medicationId, triggerAtMillis)
        val intent = buildIntent(spec)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_EVENT_ID, eventId)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_SCHEDULED_AT_MS, triggerAtMillis)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_SCHEDULE_TIME, spec.scheduleTime)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_MEDICATION_NAME, spec.medicationName)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_DOSAGE, spec.dosage)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_PATIENT_NAME, spec.patientName)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_PATIENT_UID, spec.patientUid)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_MEDICATION_ID, spec.medicationId)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_ROLE, spec.role.name)

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
                triggerAtMillis,
                pending
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pending
            )
        }
    }

    private fun buildIntent(spec: MedicationReminderSpec): Intent {
        val data = Uri.parse("senior://med_reminder/${requestCode(spec)}")
        return Intent(context, MedicationReminderAlarmReceiver::class.java)
            .setAction(MedicationReminderAlarmReceiver.ACTION_REMINDER)
            .setData(data)
    }

    private fun requestCode(spec: MedicationReminderSpec): Int {
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
