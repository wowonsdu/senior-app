package zdrowy.senior.io.ui.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.core.context.GlobalContext
import zdrowy.senior.io.domain.settings.reminders.ConfirmMedicationReminderTakenUseCase
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderEvent
import zdrowy.senior.io.domain.settings.reminders.MedicationReminderTakenSource
import zdrowy.senior.io.domain.settings.reminders.UpsertMedicationReminderEventUseCase

class MedicationReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_REMINDER -> handleReminder(context, intent)
            ACTION_CONFIRM_TAKEN -> handleConfirmTaken(context, intent)
        }
    }

    private fun handleReminder(context: Context, intent: Intent) {
        val roleName = intent.getStringExtra(EXTRA_ROLE) ?: return
        val role = runCatching { ReminderRole.valueOf(roleName) }.getOrNull() ?: return
        val patientUid = intent.getStringExtra(EXTRA_PATIENT_UID) ?: return
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME).orEmpty()
        val dosage = intent.getStringExtra(EXTRA_DOSAGE).orEmpty()
        val scheduleTime = intent.getStringExtra(EXTRA_SCHEDULE_TIME).orEmpty()
        val scheduledAtMs = intent.getLongExtra(EXTRA_SCHEDULED_AT_MS, 0L)
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty().ifBlank { return }
        val patientName = intent.getStringExtra(EXTRA_PATIENT_NAME).orEmpty()

        val notifier = MedicationReminderNotifier(context.applicationContext)
        notifier.showReminder(
            role = role,
            patientName = patientName,
            medicationName = medicationName,
            dosage = dosage,
            eventId = eventId,
            patientUid = patientUid,
            medicationId = medicationId,
            scheduleTime = scheduleTime,
            scheduledAtMs = scheduledAtMs
        )

        val koin = GlobalContext.get()
        val upsertUseCase: UpsertMedicationReminderEventUseCase = koin.get()
        val scheduler: MedicationReminderAlarmScheduler = koin.get()

        val event = MedicationReminderEvent(
            id = eventId,
            patientUid = patientUid,
            medicationId = medicationId,
            scheduledAtMs = scheduledAtMs,
            medicationName = medicationName,
            dosage = dosage,
            scheduleTime = scheduleTime,
            isTaken = false,
            takenAtMs = null,
            takenSource = null
        )

        val pendingResult = goAsync()
        upsertUseCase(patientUid, event)
            .subscribeOn(Schedulers.io())
            .subscribe({
                scheduler.scheduleNextOccurrence(
                    MedicationReminderSpec(
                        role = role,
                        patientUid = patientUid,
                        patientName = patientName,
                        medicationId = medicationId,
                        medicationName = medicationName,
                        dosage = dosage,
                        scheduleTime = scheduleTime
                    )
                )
                pendingResult.finish()
            }, {
                scheduler.scheduleNextOccurrence(
                    MedicationReminderSpec(
                        role = role,
                        patientUid = patientUid,
                        patientName = patientName,
                        medicationId = medicationId,
                        medicationName = medicationName,
                        dosage = dosage,
                        scheduleTime = scheduleTime
                    )
                )
                pendingResult.finish()
            })
    }

    private fun handleConfirmTaken(context: Context, intent: Intent) {
        val patientUid = intent.getStringExtra(EXTRA_PATIENT_UID) ?: return
        val medicationId = intent.getStringExtra(EXTRA_MEDICATION_ID) ?: return
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME).orEmpty()
        val dosage = intent.getStringExtra(EXTRA_DOSAGE).orEmpty()
        val scheduleTime = intent.getStringExtra(EXTRA_SCHEDULE_TIME).orEmpty()
        val scheduledAtMs = intent.getLongExtra(EXTRA_SCHEDULED_AT_MS, 0L)
        val eventId = intent.getStringExtra(EXTRA_EVENT_ID).orEmpty().ifBlank { return }

        val event = MedicationReminderEvent(
            id = eventId,
            patientUid = patientUid,
            medicationId = medicationId,
            scheduledAtMs = scheduledAtMs,
            medicationName = medicationName,
            dosage = dosage,
            scheduleTime = scheduleTime,
            isTaken = false,
            takenAtMs = null,
            takenSource = null
        )

        val pendingResult = goAsync()
        val notifier = MedicationReminderNotifier(context.applicationContext)
        val confirmUseCase: ConfirmMedicationReminderTakenUseCase = GlobalContext.get().get()
        confirmUseCase(
            patientUid = patientUid,
            event = event,
            confirmedAtMs = System.currentTimeMillis(),
            source = MedicationReminderTakenSource.PATIENT_NOTIFICATION_ACTION
        )
            .subscribeOn(Schedulers.io())
            .subscribe({
                notifier.cancelReminder(ReminderRole.PATIENT, eventId)
                pendingResult.finish()
            }, {
                pendingResult.finish()
            })
    }

    companion object {
        const val ACTION_REMINDER = "zdrowy.senior.io.MED_REMINDER"
        const val ACTION_CONFIRM_TAKEN = "zdrowy.senior.io.MED_REMINDER_CONFIRM_TAKEN"
        const val EXTRA_ROLE = "extra_role"
        const val EXTRA_PATIENT_UID = "extra_patient_uid"
        const val EXTRA_PATIENT_NAME = "extra_patient_name"
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_DOSAGE = "extra_dosage"
        const val EXTRA_SCHEDULE_TIME = "extra_schedule_time"
        const val EXTRA_SCHEDULED_AT_MS = "extra_scheduled_at_ms"
        const val EXTRA_EVENT_ID = "extra_event_id"
    }
}
