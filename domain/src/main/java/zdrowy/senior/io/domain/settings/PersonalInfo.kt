package zdrowy.senior.io.domain.settings

data class PersonalInfo(
    val firstName: String,
    val lastName: String,
    val pesel: String,
    val address: String,
    val phone: String
)
