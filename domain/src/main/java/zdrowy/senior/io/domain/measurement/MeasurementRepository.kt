package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MeasurementRepository {
    fun addMeasurement(patientId: String, type: MeasurementType, valueRaw: String): Completable
    fun observeMeasurements(patientId: String): Observable<List<Measurement>>
}
