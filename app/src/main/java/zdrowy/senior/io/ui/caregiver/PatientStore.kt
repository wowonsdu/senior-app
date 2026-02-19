package zdrowy.senior.io.ui.caregiver

data class PatientItem(
    val id: String,
    val name: String,
    val phone: String?
)

interface PatientStore {
    fun getPatients(): List<PatientItem>
    fun addPatient(name: String, phone: String?): PatientItem
}

