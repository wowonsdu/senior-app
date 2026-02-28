package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface PatientAccountLinkRepository {
    fun observeLinkedPatientUid(accountUid: String): Observable<String>
    fun getLinkedPatientUid(accountUid: String): Single<String>
    fun upsertPatientLink(accountUid: String, patientUid: String, linkCode: String? = null): Completable
    fun removePatientLink(accountUid: String): Completable
    fun removeLinksByPatientUid(patientUid: String): Completable
}
