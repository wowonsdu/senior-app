package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class AddMedicineUseCase(
    private val repository: SettingsRepository
) {
    fun execute(patientId: String, name: String, dosage: String): Completable {
        return repository.addMedicine(patientId, name, dosage)
    }
}
