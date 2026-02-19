package zdrowy.senior.io.data.measurement

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.measurement.Measurement
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.MeasurementType
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemoryMeasurementRepository : MeasurementRepository {
    private val subjects = ConcurrentHashMap<String, BehaviorSubject<List<Measurement>>>()

    override fun addMeasurement(
        patientId: String,
        type: MeasurementType,
        valueRaw: String
    ): Completable {
        return Completable.fromAction {
            val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
            val current = subject.value ?: emptyList()
            val measurement = Measurement(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                type = type,
                valueRaw = valueRaw,
                createdAt = System.currentTimeMillis()
            )
            subject.onNext(listOf(measurement) + current)
        }
    }

    override fun observeMeasurements(patientId: String): Observable<List<Measurement>> {
        val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
        return subject.hide()
    }
}

