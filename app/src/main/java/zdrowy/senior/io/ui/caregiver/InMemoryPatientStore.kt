package zdrowy.senior.io.ui.caregiver

import java.util.UUID

class InMemoryPatientStore : PatientStore {
    private val patients = mutableListOf<PatientItem>()

    override fun getPatients(): List<PatientItem> {
        return patients.toList()
    }

    override fun addPatient(name: String, phone: String?): PatientItem {
        val item = PatientItem(
            id = UUID.randomUUID().toString(),
            name = name,
            phone = phone
        )
        patients.add(item)
        return item
    }
}

