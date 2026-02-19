package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable

class ToggleMedicationNotificationsUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(id: String, enabled: Boolean): Completable =
        repository.toggleMedicationNotifications(id, enabled)
}
