package zdrowy.senior.io.data.user

import com.google.firebase.auth.FirebaseAuth
import zdrowy.senior.io.domain.user.CurrentUserUidProvider

class FirebaseCurrentUserUidProvider : CurrentUserUidProvider {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun requireUid(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }
}
