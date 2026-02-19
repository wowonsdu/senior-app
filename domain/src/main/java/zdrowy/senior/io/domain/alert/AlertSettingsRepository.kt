package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Observable

interface AlertSettingsRepository {
    fun observeSettings(patientId: String): Observable<List<AlertSetting>>
    fun updateSettings(patientId: String, settings: List<AlertSetting>)
}
