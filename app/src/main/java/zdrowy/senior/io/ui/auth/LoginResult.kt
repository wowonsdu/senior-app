package zdrowy.senior.io.ui.auth

data class LoginResult(
    val verificationId: String,
    val inviteId: String?
)
