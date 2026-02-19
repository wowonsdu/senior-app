package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateAlertCaregiversUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(type: MeasurementType, caregiverIds: List<String>): Completable =
        repository.updateAlertCaregivers(type, caregiverIds)
}
