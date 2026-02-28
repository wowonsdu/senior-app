package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Single

class CreateDependentProfileUseCase(
    private val repository: CareLinkRepository
) {
    operator fun invoke(draft: CareLinkDraft): Single<CareLink> =
        repository.createDependentProfile(draft)
}
