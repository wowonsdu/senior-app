package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable

class UpsertMedicationReminderEventUseCase(
    private val repository: MedicationReminderEventRepository
) {
    operator fun invoke(patientUid: String, event: MedicationReminderEvent): Completable =
        repository.upsertEvent(patientUid, event)
}
