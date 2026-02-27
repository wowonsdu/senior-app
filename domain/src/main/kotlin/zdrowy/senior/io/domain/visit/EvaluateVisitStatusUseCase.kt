package zdrowy.senior.io.domain.visit

class EvaluateVisitStatusUseCase {
    operator fun invoke(
        visit: Visit,
        nowMs: Long = System.currentTimeMillis()
    ): VisitComputedStatus {
        return if (visit.isCompletedManual || visit.scheduledAtMs <= nowMs) {
            VisitComputedStatus.COMPLETED
        } else {
            VisitComputedStatus.UPCOMING
        }
    }
}
