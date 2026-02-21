package zdrowy.senior.io.domain.carelink

data class CareLinkDraft(
    val firstName: String,
    val lastName: String,
    val pesel: String,
    val phoneNumber: String,
    val address: String
)
