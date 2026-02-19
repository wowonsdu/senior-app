package zdrowy.senior.io.domain.session

class GetActivePatientUseCase(
    private val sessionRepository: SessionRepository
) {
    fun execute(): String? {
        return sessionRepository.getActivePatientId()
    }
}
