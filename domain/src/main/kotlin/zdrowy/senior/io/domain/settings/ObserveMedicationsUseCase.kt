package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObserveMedicationsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Observable<List<Medication>> = repository.observeMedications()
}
