package zdrowy.senior.io.ui.patient

import zdrowy.senior.io.domain.agent.Agent
import zdrowy.senior.io.domain.settings.Disease
import zdrowy.senior.io.domain.settings.Medication
import zdrowy.senior.io.domain.settings.PersonalData
import zdrowy.senior.io.domain.settings.SettingsOverview

data class PatientSettingsUiState(
    val personalData: PersonalData,
    val diseases: List<Disease>,
    val medications: List<Medication>,
    val caregivers: List<Agent>,
    val doctors: List<Agent>
) {
    val personalFullName: String
        get() = listOf(personalData.firstName, personalData.lastName)
            .filter { it.isNotBlank() }
            .joinToString(" ")

    companion object {
        fun from(
            overview: SettingsOverview,
            caregivers: List<Agent>,
            doctors: List<Agent>
        ): PatientSettingsUiState {
            return PatientSettingsUiState(
                personalData = overview.personalData,
                diseases = overview.diseases,
                medications = overview.medications,
                caregivers = caregivers,
                doctors = doctors
            )
        }
    }
}
