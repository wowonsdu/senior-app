package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Maybe

interface SettingsRepository {
    fun getPersonalInfo(patientId: String): Maybe<PersonalInfo>
    fun updatePersonalInfo(patientId: String, info: PersonalInfo): Completable

    fun observeMedicines(patientId: String): Observable<List<Medicine>>
    fun addMedicine(patientId: String, name: String, dosage: String): Completable

    fun observeDiseases(patientId: String): Observable<List<Disease>>
    fun addDisease(patientId: String, name: String): Completable
}
