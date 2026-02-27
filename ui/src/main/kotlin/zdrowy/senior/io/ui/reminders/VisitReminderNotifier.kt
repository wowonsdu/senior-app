package zdrowy.senior.io.ui.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import zdrowy.senior.io.ui.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisitReminderNotifier(
    private val context: Context
) {
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    fun showReminder(
        eventId: String,
        patientName: String,
        visitTitle: String,
        location: String,
        scheduledAtMs: Long,
        reminderOffsetMinutes: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel()

        val dateTime = dateTimeFormat.format(Date(scheduledAtMs))
        val body = context.getString(
            R.string.visit_reminder_body,
            patientName.ifBlank { "-" },
            visitTitle,
            reminderOffsetMinutes,
            dateTime,
            location.ifBlank { "-" }
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert_bell)
            .setContentTitle(context.getString(R.string.visit_reminder_title))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            notificationId(eventId),
            notification
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.visit_reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.visit_reminder_channel_desc)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun notificationId(eventId: String): Int {
        return ("visit_reminder_$eventId").hashCode()
    }

    private companion object {
        private const val CHANNEL_ID = "visit_reminders"
    }
}
