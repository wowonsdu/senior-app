package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class UpdatePersonalInfoUseCase(
    private val repository: SettingsRepository
) {
    fun execute(patientId: String, info: PersonalInfo): Completable {
        return repository.updatePersonalInfo(patientId, info)
    }
}
