package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Single

class GetAlertConfigUseCase(
    private val repository: AlertRepository
) {
    operator fun invoke(): Single<AlertConfig> = repository.getAlertConfig()
}
