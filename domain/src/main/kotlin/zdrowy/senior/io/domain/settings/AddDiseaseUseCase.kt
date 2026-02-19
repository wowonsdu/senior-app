package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Single

class AddDiseaseUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(draft: DiseaseDraft): Single<String> = repository.addDisease(draft)
}
