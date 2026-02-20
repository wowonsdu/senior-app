package zdrowy.senior.io.domain.settings

data class PersonalData(
    val firstName: String,
    val lastName: String,
    val pesel: String,
    val phoneNumber: String,
    val email: String,
    val address: String
)
