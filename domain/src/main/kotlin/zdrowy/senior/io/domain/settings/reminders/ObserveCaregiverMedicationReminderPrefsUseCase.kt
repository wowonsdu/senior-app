package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Observable

class ObserveCaregiverMedicationReminderPrefsUseCase(
    private val repository: CaregiverMedicationReminderPrefsRepository
) {
    operator fun invoke(): Observable<Map<String, Boolean>> = repository.observePrefs()
}
