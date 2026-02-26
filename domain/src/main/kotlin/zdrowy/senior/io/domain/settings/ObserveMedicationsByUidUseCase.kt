package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObserveMedicationsByUidUseCase(
    private val repository: MedicationByUidRepository
) {
    operator fun invoke(patientUid: String): Observable<List<Medication>> =
        repository.observeMedications(patientUid)
}
