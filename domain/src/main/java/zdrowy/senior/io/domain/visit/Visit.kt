package zdrowy.senior.io.domain.visit

data class Visit(
    val id: String,
    val patientId: String,
    val title: String,
    val dateTime: String,
    val notes: String?
)
