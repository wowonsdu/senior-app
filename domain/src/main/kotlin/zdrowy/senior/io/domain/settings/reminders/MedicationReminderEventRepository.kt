package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MedicationReminderEventRepository {
    fun upsertEvent(patientUid: String, event: MedicationReminderEvent): Completable
    fun confirmTaken(
        patientUid: String,
        event: MedicationReminderEvent,
        confirmedAtMs: Long,
        source: MedicationReminderTakenSource
    ): Completable
    fun observeRecentEvents(patientUid: String, limit: Int): Observable<List<MedicationReminderEvent>>
}
