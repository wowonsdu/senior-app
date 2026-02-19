package zdrowy.senior.io.domain.agent

import io.reactivex.rxjava3.core.Completable

interface NotificationRepository {
    fun sendAccessCodeSms(agentId: String, accessCode: AccessCode): Completable
}
