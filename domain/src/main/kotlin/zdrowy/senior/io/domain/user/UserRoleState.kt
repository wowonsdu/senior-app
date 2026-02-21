package zdrowy.senior.io.domain.user

sealed class UserRoleState {
    data class Available(val role: UserRole) : UserRoleState()
    object Missing : UserRoleState()
}
