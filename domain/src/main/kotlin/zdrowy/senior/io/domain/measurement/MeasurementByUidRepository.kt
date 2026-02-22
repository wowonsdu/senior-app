package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable

interface MeasurementByUidRepository {
    fun observeRecentMeasurements(
        patientUid: String,
        limit: Int,
        types: List<MeasurementType>? = null
    ): Observable<List<Measurement>>
}
