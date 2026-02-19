package zdrowy.senior.io.domain.session

class GetSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    fun execute(): UserSession? {
        return sessionRepository.getSession()
    }
}
