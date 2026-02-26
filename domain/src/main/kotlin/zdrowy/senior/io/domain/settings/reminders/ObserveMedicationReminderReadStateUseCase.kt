package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Observable

class ObserveMedicationReminderReadStateUseCase(
    private val repository: MedicationReminderReadStateRepository
) {
    operator fun invoke(patientUid: String): Observable<MedicationReminderReadState> =
        repository.observeReadState(patientUid)
}
