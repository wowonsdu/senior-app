package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable

class UpdateNotificationSettingsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(settings: NotificationSettings): Completable =
        repository.updateNotificationSettings(settings)
}

