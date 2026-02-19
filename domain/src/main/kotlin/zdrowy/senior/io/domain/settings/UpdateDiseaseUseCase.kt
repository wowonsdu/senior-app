package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class UpdateDiseaseUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(id: String, update: DiseaseUpdate): Completable =
        repository.updateDisease(id, update)
}
