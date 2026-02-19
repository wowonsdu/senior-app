package zdrowy.senior.io.domain.session

class SetActivePatientUseCase(
    private val sessionRepository: SessionRepository
) {
    fun execute(patientId: String?) {
        sessionRepository.setActivePatientId(patientId)
    }
}
