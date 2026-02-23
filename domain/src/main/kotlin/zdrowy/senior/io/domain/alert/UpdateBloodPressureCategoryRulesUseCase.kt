package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import zdrowy.senior.io.domain.measurement.BloodPressureSeverity
import zdrowy.senior.io.domain.measurement.BloodPressureStandard

class UpdateBloodPressureCategoryRulesUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(
        enabled: Boolean,
        threshold: BloodPressureSeverity,
        cooldownMinutes: Int,
        standard: BloodPressureStandard
    ): Completable = repository.updateBloodPressureCategoryRules(
        enabled = enabled,
        threshold = threshold,
        cooldownMinutes = cooldownMinutes,
        standard = standard
    )
}

