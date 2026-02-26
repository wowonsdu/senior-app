package zdrowy.senior.io.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MedicationReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val cache = MedicationReminderAlarmCache(context.applicationContext)
        val scheduler = MedicationReminderAlarmScheduler(context.applicationContext, cache)
        scheduler.rescheduleFromCache(ReminderRole.PATIENT)
        scheduler.rescheduleFromCache(ReminderRole.CAREGIVER)
    }
}
