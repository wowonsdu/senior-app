package zdrowy.senior.io.data.notification

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.agent.AccessCode
import zdrowy.senior.io.domain.agent.NotificationRepository

class NoOpNotificationRepository : NotificationRepository {
    override fun sendAccessCodeSms(agentId: String, accessCode: AccessCode): Completable {
        return Completable.complete()
    }

    override fun notifyAgents(agentIds: List<String>, message: String): Completable {
        return Completable.complete()
    }
}
