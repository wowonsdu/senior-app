package zdrowy.senior.io.domain.settings

data class SettingsOverview(
    val personalData: PersonalData,
    val diseases: List<Disease>,
    val medications: List<Medication>
)
