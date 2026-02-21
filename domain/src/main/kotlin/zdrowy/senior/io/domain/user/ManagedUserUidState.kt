package zdrowy.senior.io.domain.user

sealed class ManagedUserUidState {
    data class Available(val role: UserRole, val uid: String) : ManagedUserUidState()
    object MissingActivePatient : ManagedUserUidState()
}
