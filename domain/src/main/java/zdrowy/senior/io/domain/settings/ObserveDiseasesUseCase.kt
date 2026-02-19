package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObserveDiseasesUseCase(
    private val repository: SettingsRepository
) {
    fun execute(patientId: String): Observable<List<Disease>> {
        return repository.observeDiseases(patientId)
    }
}
