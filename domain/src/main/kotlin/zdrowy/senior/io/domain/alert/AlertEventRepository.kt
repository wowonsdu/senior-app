package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.measurement.MeasurementType

interface AlertEventRepository {
    fun addEvent(draft: AlertEventDraft): Single<String>

    fun getLastEvent(type: MeasurementType, severity: AlertSeverity): Maybe<AlertEvent>

    fun observeRecentEvents(
        type: MeasurementType,
        limit: Int,
        severity: AlertSeverity? = null
    ): Observable<List<AlertEvent>>
}

