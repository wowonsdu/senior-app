package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable

class UpdateBloodPressureCriticalThresholdsUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ): Completable = repository.updateBloodPressureCriticalThresholds(
        systolicMin = systolicMin,
        systolicMax = systolicMax,
        diastolicMin = diastolicMin,
        diastolicMax = diastolicMax
    )
}

