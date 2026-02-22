package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Single

class GetCareLinkCodeInfoUseCase(
    private val repository: CareLinkRepository
) {
    operator fun invoke(code: String): Single<CareLinkCodeInfo> = repository.getLinkCodeInfo(code)
}
