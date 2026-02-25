package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable

interface AccountRepository {
    fun deleteCurrentUserAccount(): Completable
    fun deleteUserData(uid: String): Completable
}
