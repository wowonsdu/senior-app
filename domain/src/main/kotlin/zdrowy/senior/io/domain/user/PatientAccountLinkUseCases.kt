package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class ObserveCurrentPatientLinkUseCase(
    private val repository: PatientAccountLinkRepository,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(): Observable<String> {
        val accountUid = currentUserUidProvider.requireUid()
        return repository.observeLinkedPatientUid(accountUid)
    }
}

class GetCurrentPatientLinkUseCase(
    private val repository: PatientAccountLinkRepository,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(): Single<String> {
        val accountUid = currentUserUidProvider.requireUid()
        return repository.getLinkedPatientUid(accountUid)
    }
}

class UpsertCurrentPatientLinkUseCase(
    private val repository: PatientAccountLinkRepository,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(patientUid: String, linkCode: String? = null): Completable {
        val accountUid = currentUserUidProvider.requireUid()
        return repository.upsertPatientLink(accountUid, patientUid, linkCode)
    }
}

class RemoveCurrentPatientLinkUseCase(
    private val repository: PatientAccountLinkRepository,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(): Completable {
        val accountUid = currentUserUidProvider.requireUid()
        return repository.removePatientLink(accountUid)
    }
}

class RemovePatientLinksByPatientUidUseCase(
    private val repository: PatientAccountLinkRepository
) {
    operator fun invoke(patientUid: String): Completable = repository.removeLinksByPatientUid(patientUid)
}
