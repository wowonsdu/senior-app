package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Maybe

class GetPersonalInfoUseCase(
    private val repository: SettingsRepository
) {
    fun execute(patientId: String): Maybe<PersonalInfo> {
        return repository.getPersonalInfo(patientId)
    }
}
