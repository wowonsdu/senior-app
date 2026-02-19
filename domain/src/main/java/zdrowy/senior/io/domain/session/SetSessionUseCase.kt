package zdrowy.senior.io.domain.session

class SetSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    fun execute(session: UserSession) {
        sessionRepository.setSession(session)
    }
}
