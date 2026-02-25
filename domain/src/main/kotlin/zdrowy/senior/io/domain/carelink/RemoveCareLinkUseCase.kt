package zdrowy.senior.io.domain.carelink

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.user.CurrentUserUidProvider

class RemoveCareLinkUseCase(
    private val repository: CareLinkRepository,
    private val currentUserUidProvider: CurrentUserUidProvider
) {
    operator fun invoke(patientUid: String): Completable {
        val caregiverUid = currentUserUidProvider.requireUid()
        return repository.removeCareLink(patientUid, caregiverUid)
    }
}
