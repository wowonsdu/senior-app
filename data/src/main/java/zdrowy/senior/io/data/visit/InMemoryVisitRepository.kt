package zdrowy.senior.io.data.visit

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.visit.Visit
import zdrowy.senior.io.domain.visit.VisitRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryVisitRepository : VisitRepository {
    private val subjects = ConcurrentHashMap<String, BehaviorSubject<List<Visit>>>()

    override fun observeVisits(patientId: String): Observable<List<Visit>> {
        val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
        return subject.hide()
    }

    override fun addVisit(patientId: String, title: String, dateTime: String, notes: String?): Completable {
        return Completable.fromAction {
            val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
            val current = subject.value ?: emptyList()
            val item = Visit(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                title = title,
                dateTime = dateTime,
                notes = notes
            )
            subject.onNext(listOf(item) + current)
        }
    }
}

