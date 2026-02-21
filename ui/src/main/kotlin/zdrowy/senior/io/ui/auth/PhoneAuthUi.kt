package zdrowy.senior.io.ui.auth

import androidx.navigation.NavOptions
import zdrowy.senior.io.ui.R

object PhoneAuthUi {
    const val ARG_VERIFICATION_ID = "verificationId"
    const val ARG_PHONE_E164 = "phoneE164"
    const val ARG_PENDING_CARE_LINK_CODE = "pendingCareLinkCode"

    fun navOptionsPopToRoleSelect(): NavOptions =
        NavOptions.Builder()
            .setPopUpTo(R.id.roleSelectFragment, false)
            .build()
}

fun normalizePhoneNumberPl(raw: String): String? {
    val cleaned = raw.replace(" ", "").replace("-", "")
    if (cleaned.isBlank()) return null
    if (cleaned.startsWith("+")) {
        return if (cleaned.drop(1).all { it.isDigit() }) cleaned else null
    }
    val digitsOnly = cleaned.filter { it.isDigit() }
    return if (digitsOnly.length == 9) "+48$digitsOnly" else null
}
