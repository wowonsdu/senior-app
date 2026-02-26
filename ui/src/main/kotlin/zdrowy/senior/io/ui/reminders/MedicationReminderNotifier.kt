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

class MedicationReminderNotifier(
    private val context: Context
) {
    fun showReminder(
        role: ReminderRole,
        patientName: String,
        medicationName: String,
        dosage: String
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel()

        val title = when (role) {
            ReminderRole.PATIENT -> context.getString(R.string.med_reminder_title_patient)
            ReminderRole.CAREGIVER -> context.getString(R.string.med_reminder_title_caregiver)
        }
        val body = when (role) {
            ReminderRole.PATIENT -> context.getString(
                R.string.med_reminder_body_patient,
                medicationName,
                dosage
            )
            ReminderRole.CAREGIVER -> context.getString(
                R.string.med_reminder_body_caregiver,
                patientName.ifBlank { "-" },
                medicationName,
                dosage
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert_bell)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            ("med_reminder_" + role.name + "_" + medicationName + "_" + dosage).hashCode(),
            notification
        )
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val name = context.getString(R.string.med_reminder_channel_name)
        val description = context.getString(R.string.med_reminder_channel_desc)
        val channel = NotificationChannel(
            CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = description
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private companion object {
        private const val CHANNEL_ID = "med_reminders"
    }
}
