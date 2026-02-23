package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Single

class AddAlertEventUseCase(
    private val repository: AlertEventRepository
) {
    operator fun invoke(draft: AlertEventDraft): Single<String> = repository.addEvent(draft)
}

