package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Single

class ConsumeCareLinkCodeUseCase(
    private val repository: CareLinkRepository
) {
    operator fun invoke(code: String): Single<CareLink> = repository.consumeLinkCode(code)
}
