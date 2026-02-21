package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

class SetActivePatientUseCase(
    private val context: ActivePatientContext
) {
    operator fun invoke(uid: String): Completable = context.setActivePatientUid(uid)
}

class ClearActivePatientUseCase(
    private val context: ActivePatientContext
) {
    operator fun invoke(): Completable = context.clearActivePatientUid()
}

class ObserveActivePatientUseCase(
    private val context: ActivePatientContext
) {
    operator fun invoke(): Observable<String> = context.observeActivePatientUid()
}
