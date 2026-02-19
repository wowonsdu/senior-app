package zdrowy.senior.io.data.settings

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.domain.settings.DiseaseDraft
import zdrowy.senior.io.domain.settings.DiseaseUpdate
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.MedicationDraft
import zdrowy.senior.io.domain.settings.MedicationUpdate
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.SettingsRepository
import java.util.UUID

class InMemorySettingsRepository : SettingsRepository {
    private var personalData: PersonalData? = null
    private val diseases = mutableListOf<Disease>()
    private val medications = mutableListOf<Medication>()

    init {
        seedData()
    }

    override fun getPersonalData(): Single<PersonalData> {
        val fallback = PersonalData(
            fullName = "",
            pesel = "",
            phoneNumber = "",
            email = "",
            address = ""
        )
        return Single.just(personalData ?: fallback)
    }

    override fun upsertPersonalData(data: PersonalData): Completable {
        personalData = data
        return Completable.complete()
    }

    override fun addDisease(draft: DiseaseDraft): Single<String> {
        val id = UUID.randomUUID().toString()
        diseases.add(
            Disease(
                id = id,
                name = draft.name,
                severity = draft.severity,
                notes = draft.notes
            )
        )
        return Single.just(id)
    }

    override fun updateDisease(id: String, update: DiseaseUpdate): Completable {
        val index = diseases.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = diseases[index]
            diseases[index] = current.copy(
                name = update.name ?: current.name,
                severity = update.severity ?: current.severity,
                notes = update.notes ?: current.notes
            )
        }
        return Completable.complete()
    }

    override fun removeDisease(id: String): Completable {
        diseases.removeAll { it.id == id }
        return Completable.complete()
    }

    override fun listDiseases(): Single<List<Disease>> = Single.just(diseases.toList())

    override fun addMedication(draft: MedicationDraft): Single<String> {
        val id = UUID.randomUUID().toString()
        medications.add(
            Medication(
                id = id,
                name = draft.name,
                dosage = draft.dosage,
                schedule = draft.schedule,
                notificationsEnabled = draft.notificationsEnabled
            )
        )
        return Single.just(id)
    }

    override fun updateMedication(id: String, update: MedicationUpdate): Completable {
        val index = medications.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = medications[index]
            medications[index] = current.copy(
                name = update.name ?: current.name,
                dosage = update.dosage ?: current.dosage,
                schedule = update.schedule ?: current.schedule,
                notificationsEnabled = update.notificationsEnabled ?: current.notificationsEnabled
            )
        }
        return Completable.complete()
    }

    override fun removeMedication(id: String): Completable {
        medications.removeAll { it.id == id }
        return Completable.complete()
    }

    override fun listMedications(): Single<List<Medication>> = Single.just(medications.toList())

    override fun toggleMedicationNotifications(id: String, enabled: Boolean): Completable {
        val index = medications.indexOfFirst { it.id == id }
        if (index >= 0) {
            val current = medications[index]
            medications[index] = current.copy(notificationsEnabled = enabled)
        }
        return Completable.complete()
    }

    private fun seedData() {
        personalData = PersonalData(
            fullName = "Janina Kowalska",
            pesel = "2343242",
            phoneNumber = "+48 500 111 222",
            email = "janina.kowalska@example.com",
            address = "Warszawa, ul. Sloneczna 12"
        )

        diseases += Disease(
            id = UUID.randomUUID().toString(),
            name = "Nadcisnienie",
            severity = "Umiarkowane",
            notes = "Kontrola co 3 miesiace"
        )
        diseases += Disease(
            id = UUID.randomUUID().toString(),
            name = "Cukrzyca",
            severity = "Lekka",
            notes = "Dieta i ruch"
        )

        medications += Medication(
            id = UUID.randomUUID().toString(),
            name = "Metformina",
            dosage = "500 mg",
            schedule = "Rano i wieczorem",
            notificationsEnabled = true
        )
        medications += Medication(
            id = UUID.randomUUID().toString(),
            name = "Amlodypina",
            dosage = "5 mg",
            schedule = "Rano",
            notificationsEnabled = false
        )
    }
}
