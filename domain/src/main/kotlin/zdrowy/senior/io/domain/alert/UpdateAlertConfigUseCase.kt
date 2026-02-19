package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable

class UpdateAlertConfigUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(config: AlertConfig): Completable = repository.updateAlertConfig(config)
}
