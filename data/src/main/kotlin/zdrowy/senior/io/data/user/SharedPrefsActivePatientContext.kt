package zdrowy.senior.io.data.user

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.user.ActivePatientContext

class SharedPrefsActivePatientContext(
    appContext: Context
) : ActivePatientContext {
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val subject = BehaviorSubject.createDefault(readUid())

    override fun getActivePatientUid(): String? {
        val value = subject.value
        return if (value.isNullOrBlank()) null else value
    }

    override fun observeActivePatientUid(): Observable<String> = subject.hide()

    override fun setActivePatientUid(uid: String): Completable {
        val value = uid.trim()
        return Completable.fromAction {
            prefs.edit().putString(KEY_ACTIVE_PATIENT_UID, value).apply()
            subject.onNext(value)
        }
    }

    override fun clearActivePatientUid(): Completable {
        return Completable.fromAction {
            prefs.edit().remove(KEY_ACTIVE_PATIENT_UID).apply()
            subject.onNext("")
        }
    }

    private fun readUid(): String {
        return prefs.getString(KEY_ACTIVE_PATIENT_UID, "").orEmpty()
    }

    private companion object {
        const val PREFS_NAME = "session_prefs"
        const val KEY_ACTIVE_PATIENT_UID = "active_patient_uid"
    }
}
