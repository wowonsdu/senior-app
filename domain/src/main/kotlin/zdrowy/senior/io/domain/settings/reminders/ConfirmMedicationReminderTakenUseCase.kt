package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable

class ConfirmMedicationReminderTakenUseCase(
    private val repository: MedicationReminderEventRepository
) {
    operator fun invoke(
        patientUid: String,
        event: MedicationReminderEvent,
        confirmedAtMs: Long,
        source: MedicationReminderTakenSource
    ): Completable = repository.confirmTaken(patientUid, event, confirmedAtMs, source)
}
