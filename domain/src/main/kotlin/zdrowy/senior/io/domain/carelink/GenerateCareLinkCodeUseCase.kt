package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Single

class GenerateCareLinkCodeUseCase(
    private val repository: CareLinkRepository
) {
    operator fun invoke(
        type: CareLinkCodeType,
        ttlSeconds: Long,
        draft: CareLinkDraft? = null,
        patientUid: String? = null
    ): Single<CareLinkCode> =
        repository.generateLinkCode(type, ttlSeconds, draft, patientUid)
}
