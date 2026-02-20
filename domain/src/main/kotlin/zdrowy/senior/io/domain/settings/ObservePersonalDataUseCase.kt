package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObservePersonalDataUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Observable<PersonalData> = repository.observePersonalData()
}
