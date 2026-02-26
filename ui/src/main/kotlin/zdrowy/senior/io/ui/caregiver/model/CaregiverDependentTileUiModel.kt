package zdrowy.senior.io.ui.caregiver.model

data class CaregiverDependentTileUiModel(
    val uid: String,
    val fullName: String,
    val phone: String,
    val avatar: String,
    val unreadCount: Int,
    val isSelf: Boolean,
    val reminderEnabled: Boolean,
    val showReminderToggle: Boolean
)
