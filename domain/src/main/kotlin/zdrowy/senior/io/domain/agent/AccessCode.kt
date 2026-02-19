package zdrowy.senior.io.domain.agent

data class AccessCode(
    val code: String,
    val expiresAt: Long?
)
