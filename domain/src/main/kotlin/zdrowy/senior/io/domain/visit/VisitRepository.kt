package zdrowy.senior.io.domain.visit

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface VisitRepository {
    fun observeVisits(): Observable<List<Visit>>
    fun addVisit(draft: VisitDraft): Single<String>
    fun updateVisit(visitId: String, update: VisitUpdate): Completable
    fun removeVisit(visitId: String): Completable
    fun markVisitCompleted(visitId: String, completedAtMs: Long): Completable
}
