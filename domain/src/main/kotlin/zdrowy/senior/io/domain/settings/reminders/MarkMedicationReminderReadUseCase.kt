package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable

class MarkMedicationReminderReadUseCase(
    private val repository: MedicationReminderReadStateRepository
) {
    operator fun invoke(patientUid: String, eventId: String): Completable =
        repository.markRead(patientUid, eventId)
}
