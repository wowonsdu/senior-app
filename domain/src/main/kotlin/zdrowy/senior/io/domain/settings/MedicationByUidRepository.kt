package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

interface MedicationByUidRepository {
    fun observeMedications(patientUid: String): Observable<List<Medication>>
}
