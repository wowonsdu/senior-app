package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable

class SetCaregiverMedicationReminderPrefUseCase(
    private val repository: CaregiverMedicationReminderPrefsRepository
) {
    operator fun invoke(patientUid: String, enabled: Boolean): Completable =
        repository.setEnabled(patientUid, enabled)
}
