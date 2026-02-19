package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Single

class AddDoctorUseCase(
    private val repository: AgentRepository
) {
    operator fun invoke(draft: DoctorDraft): Single<String> = repository.addDoctor(draft)
}
