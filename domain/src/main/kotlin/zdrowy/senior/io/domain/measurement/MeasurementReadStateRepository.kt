package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MeasurementReadStateRepository {
    fun observeReadState(patientUid: String): Observable<MeasurementReadState>
    fun markMeasurementRead(patientUid: String, measurementId: String): Completable
    fun setReadMeasurements(patientUid: String, measurementIds: List<String>): Completable
}
