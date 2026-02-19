package zdrowy.senior.io.domain.session

class ClearSessionUseCase(
    private val sessionRepository: SessionRepository
) {
    fun execute() {
        sessionRepository.clearSession()
    }
}
