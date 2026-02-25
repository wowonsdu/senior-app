package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable

class DeleteCurrentUserAccountUseCase(
    private val repository: AccountRepository
) {
    operator fun invoke(): Completable = repository.deleteCurrentUserAccount()
}
