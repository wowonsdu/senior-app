package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class AddDiseaseUseCase(
    private val repository: SettingsRepository
) {
    fun execute(patientId: String, name: String): Completable {
        return repository.addDisease(patientId, name)
    }
}
