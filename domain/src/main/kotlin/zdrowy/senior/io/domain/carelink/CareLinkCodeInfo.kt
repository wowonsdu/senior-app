package zdrowy.senior.io.domain.carelink

data class CareLinkCodeInfo(
    val code: String,
    val type: CareLinkCodeType,
    val expiresAtMs: Long?,
    val draftPhoneNumber: String
)
