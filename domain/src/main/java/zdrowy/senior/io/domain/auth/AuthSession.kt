package zdrowy.senior.io.domain.auth

data class AuthSession(
    val uid: String,
    val phoneE164: String?
)
