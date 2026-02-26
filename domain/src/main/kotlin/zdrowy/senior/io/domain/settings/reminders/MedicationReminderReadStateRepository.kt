package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface MedicationReminderReadStateRepository {
    fun observeReadState(patientUid: String): Observable<MedicationReminderReadState>
    fun markRead(patientUid: String, eventId: String): Completable
    fun setReadEvents(patientUid: String, eventIds: List<String>): Completable
}
