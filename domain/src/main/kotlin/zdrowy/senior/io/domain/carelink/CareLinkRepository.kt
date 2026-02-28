package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface CareLinkRepository {
    fun observeCareLinks(): Observable<List<CareLink>>
    fun generateLinkCode(
        type: CareLinkCodeType,
        ttlSeconds: Long,
        draft: CareLinkDraft? = null,
        patientUid: String? = null
    ): Single<CareLinkCode>
    fun createDependentProfile(draft: CareLinkDraft): Single<CareLink>
    fun getLinkCodeInfo(code: String): Single<CareLinkCodeInfo>
    fun consumeLinkCode(code: String): Single<CareLink>
    fun ensureCaregiverContact(caregiverUid: String): Completable
    fun removeCareLink(patientUid: String, caregiverUid: String): Completable
}
