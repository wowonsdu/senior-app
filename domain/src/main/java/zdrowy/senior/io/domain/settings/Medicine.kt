package zdrowy.senior.io.domain.settings

data class Medicine(
    val id: String,
    val patientId: String,
    val name: String,
    val dosage: String
)
