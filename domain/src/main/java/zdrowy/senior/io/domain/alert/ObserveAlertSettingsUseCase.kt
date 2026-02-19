package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Observable

class ObserveAlertSettingsUseCase(
    private val repository: AlertSettingsRepository
) {
    fun execute(patientId: String): Observable<List<AlertSetting>> {
        return repository.observeSettings(patientId)
    }
}
