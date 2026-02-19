package zdrowy.senior.io.domain.settings

data class Medication(
    val id: String,
    val name: String,
    val dosage: String,
    val schedule: String,
    val notificationsEnabled: Boolean
)
