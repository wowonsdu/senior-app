package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Observable

class ObserveManagedUserUidStateUseCase(
    private val getCurrentUserRole: GetCurrentUserRoleUseCase,
    private val observeActivePatient: ObserveActivePatientUseCase,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(): Observable<ManagedUserUidState> {
        return getCurrentUserRole()
            .toObservable()
            .switchMap { role ->
                if (role == UserRole.PATIENT) {
                    val uid = currentUserUidProvider.requireUid()
                    Observable.just(ManagedUserUidState.Available(role, uid))
                } else {
                    observeActivePatient()
                        .map { it.trim() }
                        .map { uid ->
                            if (uid.isBlank()) {
                                ManagedUserUidState.MissingActivePatient
                            } else {
                                ManagedUserUidState.Available(role, uid)
                            }
                        }
                        .distinctUntilChanged()
                }
            }
    }
}
