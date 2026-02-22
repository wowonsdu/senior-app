package zdrowy.senior.io.domain.measurement

import io.reactivex.rxjava3.core.Observable

class ObserveRecentMeasurementsByUidUseCase(
    private val repository: MeasurementByUidRepository
) {
    operator fun invoke(
        patientUid: String,
        limit: Int,
        types: List<MeasurementType>? = null
    ): Observable<List<Measurement>> {
        return repository.observeRecentMeasurements(patientUid, limit, types)
    }
}
