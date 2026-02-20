package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObserveDiseasesUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Observable<List<Disease>> = repository.observeDiseases()
}
