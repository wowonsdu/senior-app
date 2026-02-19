package zdrowy.senior.io.data.alert

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.alert.AlertSettingsRepository
import zdrowy.senior.io.domain.measurement.MeasurementType
import java.util.concurrent.ConcurrentHashMap

class InMemoryAlertSettingsRepository : AlertSettingsRepository {
    private val subjects = ConcurrentHashMap<String, BehaviorSubject<List<AlertSetting>>>()

    override fun observeSettings(patientId: String): Observable<List<AlertSetting>> {
        val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(defaultSettings()) }
        return subject.hide()
    }

    override fun updateSettings(patientId: String, settings: List<AlertSetting>) {
        val subject = subjects.getOrPut(patientId) { BehaviorSubject.createDefault(defaultSettings()) }
        subject.onNext(settings)
    }

    private fun defaultSettings(): List<AlertSetting> {
        return listOf(
            AlertSetting(MeasurementType.GLUCOSE, true, "70", "180"),
            AlertSetting(MeasurementType.INSULIN, true, "5", "15"),
            AlertSetting(MeasurementType.PRESSURE, true, "90", "140"),
            AlertSetting(MeasurementType.PULSE, true, "60", "100")
        )
    }
}

