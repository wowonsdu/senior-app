package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Single

class ListDiseasesUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Single<List<Disease>> = repository.listDiseases()
}
