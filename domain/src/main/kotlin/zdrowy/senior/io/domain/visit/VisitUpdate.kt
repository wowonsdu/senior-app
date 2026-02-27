package zdrowy.senior.io.domain.visit

data class VisitUpdate(
    val patientUid: String?,
    val title: String?,
    val scheduledAtMs: Long?,
    val location: String?,
    val notes: String?,
    val reminderEnabled: Boolean?,
    val reminderOffsetMinutes: Int?,
    val isCompletedManual: Boolean?,
    val completedAtMs: Long?
)
