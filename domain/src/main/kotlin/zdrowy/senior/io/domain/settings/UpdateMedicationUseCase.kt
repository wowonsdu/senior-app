package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class UpdateMedicationUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(id: String, update: MedicationUpdate): Completable =
        repository.updateMedication(id, update)
}
