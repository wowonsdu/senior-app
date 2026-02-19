package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateCriticalThresholdsUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(type: MeasurementType, min: Double?, max: Double?): Completable =
        repository.updateCriticalThresholds(type, min, max)
}
