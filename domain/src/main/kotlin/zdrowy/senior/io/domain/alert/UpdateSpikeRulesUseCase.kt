package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.MeasurementType

class UpdateSpikeRulesUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(type: MeasurementType, percent: Int, windowCount: Int): Completable =
        repository.updateSpikeRules(type, percent, windowCount)
}
