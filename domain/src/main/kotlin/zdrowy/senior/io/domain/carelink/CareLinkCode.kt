package zdrowy.senior.io.domain.carelink

data class CareLinkCode(
    val code: String,
    val expiresAt: Long?,
    val type: CareLinkCodeType
)

enum class CareLinkCodeType {
    PATIENT_TO_CAREGIVER,
    CAREGIVER_TO_PATIENT
}
