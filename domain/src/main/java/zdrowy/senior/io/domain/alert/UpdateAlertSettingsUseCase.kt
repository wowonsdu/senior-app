package zdrowy.senior.io.domain.alert

class UpdateAlertSettingsUseCase(
    private val repository: AlertSettingsRepository
) {
    fun execute(patientId: String, settings: List<AlertSetting>) {
        repository.updateSettings(patientId, settings)
    }
}
