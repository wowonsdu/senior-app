package zdrowy.senior.io.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class VisitReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val cache = VisitReminderAlarmCache(context.applicationContext)
        val scheduler = VisitReminderAlarmScheduler(context.applicationContext, cache)
        scheduler.rescheduleFromCache()
    }
}
