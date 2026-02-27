package zdrowy.senior.io.ui.reminders

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import zdrowy.senior.io.ui.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MedicationReminderNotifier(
    private val context: Context
) {
    fun showReminder(
        role: ReminderRole,
        patientName: String,
        medicationName: String,
        dosage: String,
        eventId: String,
        patientUid: String,
        medicationId: String,
        scheduleTime: String,
        scheduledAtMs: Long
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
            .apply {
                if (role == ReminderRole.PATIENT) {
                    addAction(
                        R.drawable.ic_alert_bell,
                        context.getString(R.string.med_reminder_action_taken),
                        buildConfirmTakenPendingIntent(
                            eventId = eventId,
                            patientUid = patientUid,
                            medicationId = medicationId,
                            medicationName = medicationName,
                            dosage = dosage,
                            scheduleTime = scheduleTime,
                            scheduledAtMs = scheduledAtMs
                        )
                    )
                }
            }
            .build()

        NotificationManagerCompat.from(context).notify(
            reminderNotificationId(role, eventId),
            notification
        )
    }

    fun showCaregiverTakenConfirmation(
        eventId: String,
        patientName: String,
        medicationName: String,
        dosage: String,
        takenAtMs: Long?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        ensureChannel()
        val takenTimeLabel = takenAtMs?.let { ms ->
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ms))
        }.orEmpty()
        val body = context.getString(
            R.string.med_reminder_body_caregiver_taken,
            patientName.ifBlank { "-" },
            medicationName,
            dosage,
            takenTimeLabel
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alert_bell)
            .setContentTitle(context.getString(R.string.med_reminder_title_caregiver_taken))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(
            caregiverTakenNotificationId(eventId),
            notification
        )
    }

    fun cancelReminder(role: ReminderRole, eventId: String) {
        NotificationManagerCompat.from(context).cancel(reminderNotificationId(role, eventId))
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

    private fun buildConfirmTakenPendingIntent(
        eventId: String,
        patientUid: String,
        medicationId: String,
        medicationName: String,
        dosage: String,
        scheduleTime: String,
        scheduledAtMs: Long
    ): PendingIntent {
        val intent = Intent(context, MedicationReminderAlarmReceiver::class.java)
            .setAction(MedicationReminderAlarmReceiver.ACTION_CONFIRM_TAKEN)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_EVENT_ID, eventId)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_PATIENT_UID, patientUid)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_DOSAGE, dosage)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_SCHEDULE_TIME, scheduleTime)
            .putExtra(MedicationReminderAlarmReceiver.EXTRA_SCHEDULED_AT_MS, scheduledAtMs)

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or pendingIntentFlags()
        return PendingIntent.getBroadcast(
            context,
            ("med_confirm_taken_$eventId").hashCode(),
            intent,
            flags
        )
    }

    private fun pendingIntentFlags(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
    }

    private fun reminderNotificationId(role: ReminderRole, eventId: String): Int {
        return ("med_reminder_${role.name}_$eventId").hashCode()
    }

    private fun caregiverTakenNotificationId(eventId: String): Int {
        return ("med_taken_caregiver_$eventId").hashCode()
    }

    private companion object {
        private const val CHANNEL_ID = "med_reminders"
    }
}
