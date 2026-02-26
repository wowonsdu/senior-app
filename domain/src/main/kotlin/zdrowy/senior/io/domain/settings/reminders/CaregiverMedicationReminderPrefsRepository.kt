package zdrowy.senior.io.domain.settings.reminders

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable

interface CaregiverMedicationReminderPrefsRepository {
    fun observePrefs(): Observable<Map<String, Boolean>>
    fun setEnabled(patientUid: String, enabled: Boolean): Completable
}
