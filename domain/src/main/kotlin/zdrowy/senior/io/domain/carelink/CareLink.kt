package zdrowy.senior.io.domain.carelink

data class CareLink(
    val patientUid: String,
    val caregiverUid: String,
    val status: CareLinkStatus
)

enum class CareLinkStatus {
    ACTIVE,
    PENDING
}
