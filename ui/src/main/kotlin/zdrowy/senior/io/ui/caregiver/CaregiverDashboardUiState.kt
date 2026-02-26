package zdrowy.senior.io.ui.caregiver

import zdrowy.senior.io.ui.caregiver.model.CaregiverDashboardItemUi

enum class CaregiverDashboardTab {
    NEW,
    READ
}

data class CaregiverDashboardUiState(
    val selectedTab: CaregiverDashboardTab,
    val newItems: List<CaregiverDashboardItemUi>,
    val readItems: List<CaregiverDashboardItemUi>,
    val showMissingPatient: Boolean
) {
    val newCount: Int = newItems.size

    fun visibleItems(): List<CaregiverDashboardItemUi> {
        return if (selectedTab == CaregiverDashboardTab.NEW) newItems else readItems
    }
}
