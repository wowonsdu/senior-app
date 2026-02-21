package zdrowy.senior.io.domain.user

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface UserProfileRepository {
    fun ensureCurrentUserProfile(role: UserRole): Completable
    fun getCurrentUserRole(): Single<UserRole>
}
