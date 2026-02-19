package zdrowy.senior.io.domain.settings

data class PersonalData(
    val fullName: String,
    val pesel: String,
    val phoneNumber: String,
    val email: String,
    val address: String
)
