package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable

class SetReadMedicationRemindersUseCase(
    private val repository: MedicationReminderReadStateRepository
) {
    operator fun invoke(patientUid: String, eventIds: List<String>): Completable =
        repository.setReadEvents(patientUid, eventIds)
}
