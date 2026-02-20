package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable

interface UserProfileRepository {
    fun ensureCurrentUserProfile(role: UserRole): Completable
}

