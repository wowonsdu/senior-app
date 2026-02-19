package zdrowy.senior.io.data.settings

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.domain.settings.Medicine
import zdrowy.senior.io.domain.settings.PersonalInfo
import zdrowy.senior.io.domain.settings.SettingsRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class InMemorySettingsRepository : SettingsRepository {
    private val personalInfo = ConcurrentHashMap<String, PersonalInfo>()
    private val medicines = ConcurrentHashMap<String, BehaviorSubject<List<Medicine>>>()
    private val diseases = ConcurrentHashMap<String, BehaviorSubject<List<Disease>>>()

    override fun getPersonalInfo(patientId: String): Maybe<PersonalInfo> {
        return Maybe.fromCallable { personalInfo[patientId] }
    }

    override fun updatePersonalInfo(patientId: String, info: PersonalInfo): Completable {
        return Completable.fromAction {
            personalInfo[patientId] = info
        }
    }

    override fun observeMedicines(patientId: String): Observable<List<Medicine>> {
        val subject = medicines.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
        return subject.hide()
    }

    override fun addMedicine(patientId: String, name: String, dosage: String): Completable {
        return Completable.fromAction {
            val subject = medicines.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
            val current = subject.value ?: emptyList()
            val item = Medicine(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                name = name,
                dosage = dosage
            )
            subject.onNext(listOf(item) + current)
        }
    }

    override fun observeDiseases(patientId: String): Observable<List<Disease>> {
        val subject = diseases.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
        return subject.hide()
    }

    override fun addDisease(patientId: String, name: String): Completable {
        return Completable.fromAction {
            val subject = diseases.getOrPut(patientId) { BehaviorSubject.createDefault(emptyList()) }
            val current = subject.value ?: emptyList()
            val item = Disease(
                id = UUID.randomUUID().toString(),
                patientId = patientId,
                name = name
            )
            subject.onNext(listOf(item) + current)
        }
    }
}
