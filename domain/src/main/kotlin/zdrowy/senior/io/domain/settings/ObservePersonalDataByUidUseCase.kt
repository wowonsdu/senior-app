package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObservePersonalDataByUidUseCase(
    private val repository: PersonalDataByUidRepository
) {
    operator fun invoke(uid: String): Observable<PersonalData> = repository.observePersonalData(uid)
}
