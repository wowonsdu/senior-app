package zdrowy.senior.io.ui

import zdrowy.senior.io.domain.session.UserRole

data class HomeUiState(
    val role: UserRole? = null,
    val activePatientName: String? = null,
    val measurementsText: String = "",
    val errorMessage: String? = null
)
