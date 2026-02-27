package zdrowy.senior.io.domain.visit

data class VisitDraft(
    val patientUid: String,
    val title: String,
    val scheduledAtMs: Long,
    val location: String,
    val notes: String,
    val reminderEnabled: Boolean,
    val reminderOffsetMinutes: Int?
)
