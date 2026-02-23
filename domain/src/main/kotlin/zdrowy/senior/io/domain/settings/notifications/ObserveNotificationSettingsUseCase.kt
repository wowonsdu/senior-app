package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Observable

class ObserveNotificationSettingsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(): Observable<NotificationSettings> = repository.observeNotificationSettings()
}

