package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Single

class GetSettingsOverviewUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Single<SettingsOverview> {
        return Single.zip(
            repository.getPersonalData(),
            repository.listDiseases(),
            repository.listMedications()
        ) { personalData, diseases, medications ->
            SettingsOverview(
                personalData = personalData,
                diseases = diseases,
                medications = medications
            )
        }
    }
}
