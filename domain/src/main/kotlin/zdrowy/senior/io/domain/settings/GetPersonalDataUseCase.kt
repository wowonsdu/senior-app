package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Single

class GetPersonalDataUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Single<PersonalData?> = repository.getPersonalData()
}
