package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable

class DeleteUserDataUseCase(
    private val repository: AccountRepository
) {
    operator fun invoke(uid: String): Completable = repository.deleteUserData(uid)
}
