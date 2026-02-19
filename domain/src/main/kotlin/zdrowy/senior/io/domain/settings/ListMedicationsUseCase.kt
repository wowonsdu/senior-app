package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Single

class ListMedicationsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Single<List<Medication>> = repository.listMedications()
}
