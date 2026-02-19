package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class UpsertPersonalDataUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(data: PersonalData): Completable = repository.upsertPersonalData(data)
}
