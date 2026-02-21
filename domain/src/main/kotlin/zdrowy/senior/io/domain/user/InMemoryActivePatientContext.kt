package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class InMemoryActivePatientContext : ActivePatientContext {
    private val subject = BehaviorSubject.createDefault("")

    override fun getActivePatientUid(): String? {
        val value = subject.value
        return if (value.isNullOrBlank()) null else value
    }

    override fun observeActivePatientUid(): Observable<String> = subject.hide()

    override fun setActivePatientUid(uid: String): Completable {
        return Completable.fromAction { subject.onNext(uid) }
    }

    override fun clearActivePatientUid(): Completable {
        return Completable.fromAction { subject.onNext("") }
    }
}
