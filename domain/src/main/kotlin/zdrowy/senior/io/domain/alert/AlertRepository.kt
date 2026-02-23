package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.measurement.MeasurementType

interface AlertRepository {
    fun getAlertConfig(): Single<AlertConfig>
    fun observeAlertConfig(): Observable<AlertConfig>
    fun updateAlertConfig(config: AlertConfig): Completable
    fun setAlertEnabled(type: MeasurementType, enabled: Boolean): Completable
    fun updateCriticalThresholds(type: MeasurementType, min: Double?, max: Double?): Completable
    fun updateSpikeRules(type: MeasurementType, percent: Int, windowCount: Int): Completable
    fun updateAlertChannels(type: MeasurementType, channels: Set<AlertChannel>): Completable
    fun updateAlertCaregivers(type: MeasurementType, caregiverIds: List<String>): Completable

    fun updateBloodPressureCriticalThresholds(
        systolicMin: Double?,
        systolicMax: Double?,
        diastolicMin: Double?,
        diastolicMax: Double?
    ): Completable
}
