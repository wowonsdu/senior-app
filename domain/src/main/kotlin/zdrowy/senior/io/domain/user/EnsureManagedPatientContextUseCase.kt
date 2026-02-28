package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable

class EnsureManagedPatientContextUseCase(
    private val getCurrentUserRole: GetCurrentUserRoleUseCase,
    private val getCurrentPatientLink: GetCurrentPatientLinkUseCase,
    private val setActivePatient: SetActivePatientUseCase,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(): Completable {
        return getCurrentUserRole()
            .flatMapCompletable { role ->
                if (role != UserRole.PATIENT) {
                    return@flatMapCompletable Completable.complete()
                }
                val fallbackUid = currentUserUidProvider.requireUid()
                getCurrentPatientLink()
                    .onErrorReturnItem(null)
                    .flatMapCompletable { linkedUid ->
                        val managedUid = linkedUid?.trim().orEmpty().ifBlank { fallbackUid }
                        setActivePatient(managedUid)
                    }
            }
    }
}
