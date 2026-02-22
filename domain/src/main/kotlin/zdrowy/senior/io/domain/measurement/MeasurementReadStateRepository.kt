package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MeasurementReadStateRepository {
    fun observeReadState(patientUid: String): Observable<MeasurementReadState>
    fun setLastReadAt(patientUid: String, timestampMs: Long): Completable
}
