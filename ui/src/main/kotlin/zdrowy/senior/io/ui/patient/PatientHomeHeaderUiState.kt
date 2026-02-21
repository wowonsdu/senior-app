package zdrowy.senior.io.ui.patient

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes

data class PatientHomeHeaderUiState(
    @StringRes val labelRes: Int,
    val fullName: String,
    val phone: String,
    val avatar: String,
    val action: PatientHomeHeaderAction,
    @DrawableRes val actionIconRes: Int
)
