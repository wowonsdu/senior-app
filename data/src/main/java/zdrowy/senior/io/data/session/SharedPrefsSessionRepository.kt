package zdrowy.senior.io.data.session

import android.content.SharedPreferences
import zdrowy.senior.io.domain.session.SessionRepository
import zdrowy.senior.io.domain.session.UserRole
import zdrowy.senior.io.domain.session.UserSession

class SharedPrefsSessionRepository(
    private val sharedPreferences: SharedPreferences
) : SessionRepository {

    override fun getSession(): UserSession? {
        val uid = sharedPreferences.getString(KEY_UID, null) ?: return null
        val roleName = sharedPreferences.getString(KEY_ROLE, null) ?: return null
        val role = runCatching { UserRole.valueOf(roleName) }.getOrNull() ?: return null
        val phone = sharedPreferences.getString(KEY_PHONE, null)
        return UserSession(uid, role, phone)
    }

    override fun setSession(session: UserSession) {
        sharedPreferences.edit()
            .putString(KEY_UID, session.uid)
            .putString(KEY_ROLE, session.role.name)
            .putString(KEY_PHONE, session.phoneE164)
            .apply()
    }

    override fun clearSession() {
        sharedPreferences.edit()
            .remove(KEY_UID)
            .remove(KEY_ROLE)
            .remove(KEY_PHONE)
            .remove(KEY_ACTIVE_PATIENT)
            .apply()
    }

    override fun getActivePatientId(): String? {
        return sharedPreferences.getString(KEY_ACTIVE_PATIENT, null)
    }

    override fun setActivePatientId(patientId: String?) {
        sharedPreferences.edit()
            .putString(KEY_ACTIVE_PATIENT, patientId)
            .apply()
    }

    companion object {
        private const val KEY_UID = "session_uid"
        private const val KEY_ROLE = "session_role"
        private const val KEY_PHONE = "session_phone"
        private const val KEY_ACTIVE_PATIENT = "active_patient_id"
    }
}
