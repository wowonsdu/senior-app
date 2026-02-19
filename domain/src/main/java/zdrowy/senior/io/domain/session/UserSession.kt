package zdrowy.senior.io.domain.session

data class UserSession(
    val uid: String,
    val role: UserRole,
    val phoneE164: String? = null
)
