package zdrowy.senior.io.ui.patient

import androidx.annotation.StringRes

data class PatientHomeHeaderUiState(
    @StringRes val labelRes: Int,
    val fullName: String,
    val phone: String,
    val avatar: String,
    val showChevron: Boolean,
    val canNavigateToLink: Boolean
)
