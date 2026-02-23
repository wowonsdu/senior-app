package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Completable

class EnsureCaregiverContactUseCase(
    private val repository: CareLinkRepository
) {
    operator fun invoke(caregiverUid: String): Completable =
        if (caregiverUid.isBlank()) {
            Completable.complete()
        } else {
            repository.ensureCaregiverContact(caregiverUid)
        }
}
