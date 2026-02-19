package zdrowy.senior.io.domain.session

interface SessionRepository {
    fun getSession(): UserSession?
    fun setSession(session: UserSession)
    fun clearSession()
    fun getActivePatientId(): String?
    fun setActivePatientId(patientId: String?)
}
