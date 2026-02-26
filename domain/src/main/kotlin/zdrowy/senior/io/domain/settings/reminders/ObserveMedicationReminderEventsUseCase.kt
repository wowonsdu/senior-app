package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Observable

class ObserveMedicationReminderEventsUseCase(
    private val repository: MedicationReminderEventRepository
) {
    operator fun invoke(patientUid: String, limit: Int): Observable<List<MedicationReminderEvent>> =
        repository.observeRecentEvents(patientUid, limit)
}
