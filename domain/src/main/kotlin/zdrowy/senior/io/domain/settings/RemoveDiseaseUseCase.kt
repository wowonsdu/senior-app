package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class RemoveDiseaseUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(id: String): Completable = repository.removeDisease(id)
}
