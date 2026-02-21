package zdrowy.senior.io.data.firestore

import com.google.firebase.auth.FirebaseAuth
import zdrowy.senior.io.domain.user.ActivePatientContext

class PatientUidProvider(
    private val activePatientContext: ActivePatientContext
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun requirePatientUid(): String {
        val activeUid = activePatientContext.getActivePatientUid()
        return activeUid ?: auth.currentUser?.uid ?: throw IllegalStateException("Not authenticated")
    }
}
