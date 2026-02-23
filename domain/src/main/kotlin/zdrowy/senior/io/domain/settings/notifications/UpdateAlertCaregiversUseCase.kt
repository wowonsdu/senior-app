package zdrowy.senior.io.domain.settings.notifications

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateAlertCaregiversUseCase(
    private val repository: NotificationSettingsRepository
) {
    operator fun invoke(type: MeasurementType, caregiverIds: List<String>): Completable =
        repository.updateAlertCaregivers(type, caregiverIds)
}

