package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

interface PersonalDataByUidRepository {
    fun observePersonalData(uid: String): Observable<PersonalData>
}
