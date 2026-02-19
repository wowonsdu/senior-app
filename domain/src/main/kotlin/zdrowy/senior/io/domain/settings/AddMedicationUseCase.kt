package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Single

class AddMedicationUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(draft: MedicationDraft): Single<String> = repository.addMedication(draft)
}
