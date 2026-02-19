package zdrowy.senior.io.domain.settings

data class MedicationDraft(
    val name: String,
    val dosage: String,
    val schedule: String,
    val notificationsEnabled: Boolean
)
