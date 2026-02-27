package zdrowy.senior.io.domain.visit

data class Visit(
    val id: String,
    val patientUid: String,
    val title: String,
    val scheduledAtMs: Long,
    val location: String,
    val notes: String,
    val isCompletedManual: Boolean,
    val completedAtMs: Long?,
    val reminderEnabled: Boolean,
    val reminderOffsetMinutes: Int?,
    val createdAtMs: Long,
    val updatedAtMs: Long
)
