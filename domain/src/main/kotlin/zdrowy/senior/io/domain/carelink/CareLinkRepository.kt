package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

interface CareLinkRepository {
    fun observeCareLinks(): Observable<List<CareLink>>
    fun generateLinkCode(type: CareLinkCodeType, ttlSeconds: Long): Single<CareLinkCode>
    fun consumeLinkCode(code: String): Single<CareLink>
}
