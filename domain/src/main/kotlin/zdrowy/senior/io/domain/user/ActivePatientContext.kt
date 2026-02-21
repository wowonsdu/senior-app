package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface ActivePatientContext {
    fun getActivePatientUid(): String?
    fun observeActivePatientUid(): Observable<String?>
    fun setActivePatientUid(uid: String): Completable
    fun clearActivePatientUid(): Completable
}
