package zdrowy.senior.io.domain.contact

data class Contact(
    val id: String,
    val patientId: String,
    val type: ContactType,
    val name: String,
    val phone: String?,
    val specialization: String?
)
