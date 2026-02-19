package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Observable

class ObserveMedicinesUseCase(
    private val repository: SettingsRepository
) {
    fun execute(patientId: String): Observable<List<Medicine>> {
        return repository.observeMedicines(patientId)
    }
}
