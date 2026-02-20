package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Observable

class ObserveAlertConfigUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(): Observable<AlertConfig> = repository.observeAlertConfig()
}
