package zdrowy.senior.io.domain.user

interface CurrentUserUidProvider {
    fun requireUid(): String
}
