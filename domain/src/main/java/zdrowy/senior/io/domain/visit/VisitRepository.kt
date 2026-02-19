package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface VisitRepository {
    fun observeVisits(patientId: String): Observable<List<Visit>>
    fun addVisit(patientId: String, title: String, dateTime: String, notes: String?): Completable
}
