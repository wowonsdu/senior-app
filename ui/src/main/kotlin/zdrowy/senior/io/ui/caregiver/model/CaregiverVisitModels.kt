package zdrowy.senior.io.ui.caregiver.model

enum class CaregiverVisitFilter {
    UPCOMING,
    ALL,
    COMPLETED
}

data class CaregiverVisitItemUi(
    val id: String,
    val patientUid: String,
    val patientName: String,
    val title: String,
    val scheduledAtMs: Long,
    val dateLabel: String,
    val timeLabel: String,
    val locationLabel: String,
    val reminderLabel: String?,
    val isCompleted: Boolean
)

data class CaregiverVisitDependentUi(
    val uid: String,
    val fullName: String,
    val phone: String
)
