package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Observable

interface DoctorByUidRepository {
    fun observeDoctors(patientUid: String): Observable<List<Agent>>
}
