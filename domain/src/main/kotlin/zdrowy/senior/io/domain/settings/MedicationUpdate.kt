package zdrowy.senior.io.domain.settings

data class MedicationUpdate(
    val name: String? = null,
    val dosage: String? = null,
    val schedule: String? = null,
    val notificationsEnabled: Boolean? = null
)
