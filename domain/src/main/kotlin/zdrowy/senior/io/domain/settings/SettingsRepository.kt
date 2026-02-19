package zdrowy.senior.io.domain.settings

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

interface SettingsRepository {
    fun getPersonalData(): Single<PersonalData>
    fun upsertPersonalData(data: PersonalData): Completable

    fun addDisease(draft: DiseaseDraft): Single<String>
    fun updateDisease(id: String, update: DiseaseUpdate): Completable
    fun removeDisease(id: String): Completable
    fun listDiseases(): Single<List<Disease>>

    fun addMedication(draft: MedicationDraft): Single<String>
    fun updateMedication(id: String, update: MedicationUpdate): Completable
    fun removeMedication(id: String): Completable
    fun listMedications(): Single<List<Medication>>

    fun toggleMedicationNotifications(id: String, enabled: Boolean): Completable
}
