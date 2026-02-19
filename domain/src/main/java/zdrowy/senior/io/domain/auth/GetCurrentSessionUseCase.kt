package zdrowy.senior.io.domain.auth

class GetCurrentSessionUseCase(
    private val authRepository: AuthRepository
) {
    fun execute(): AuthSession? {
        return authRepository.currentSession()
    }
}
