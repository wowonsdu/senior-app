package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable

class UpdateSugarDropRulesUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(dropDelta: Double?, dropWindowMinutes: Int?): Completable =
        repository.updateSugarDropRules(dropDelta, dropWindowMinutes)
}

