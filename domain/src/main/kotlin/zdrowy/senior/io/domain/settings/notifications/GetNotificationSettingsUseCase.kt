package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Single

class GetNotificationSettingsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(): Single<NotificationSettings> = repository.getNotificationSettings()
}

