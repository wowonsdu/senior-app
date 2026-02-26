package zdrowy.senior.io.ui.caregiver.model

enum class CaregiverDashboardItemKind {
    MEASUREMENT,
    MEDICATION
}

data class CaregiverDashboardItemUi(
    val id: String,
    val patientUid: String,
    val patientName: String,
    val typeLabel: String,
    val valueLabel: String,
    val timeLabel: String,
    val iconRes: Int,
    val iconTintRes: Int,
    val chipColorRes: Int,
    val timestamp: Long,
    val isRead: Boolean,
    val kind: CaregiverDashboardItemKind
)
