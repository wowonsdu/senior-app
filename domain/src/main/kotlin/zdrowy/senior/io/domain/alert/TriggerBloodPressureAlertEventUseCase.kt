package zdrowy.senior.io.domain.alert

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import zdrowy.senior.io.domain.measurement.ClassifyBloodPressureUseCase
import zdrowy.senior.io.domain.measurement.MeasurementRepository
import zdrowy.senior.io.domain.measurement.MeasurementType
import kotlin.math.abs

class TriggerBloodPressureAlertEventUseCase(
    private val alertRepository: AlertRepository,
    private val alertEventRepository: AlertEventRepository,
    private val measurementRepository: MeasurementRepository,
    private val classifyBloodPressure: ClassifyBloodPressureUseCase
) {
    operator fun invoke(
        measurementId: String,
        systolic: Int,
        diastolic: Int,
        timestamp: Long
    ): Completable {
        return alertRepository.getAlertConfig()
            .map { config -> config.settings.first { it.type == MeasurementType.PRESSURE } }
            .flatMapCompletable { setting ->
                if (!setting.enabled) return@flatMapCompletable Completable.complete()

                val classification = classifyBloodPressure(
                    systolic = systolic,
                    diastolic = diastolic
                )

                val thresholdReasons = buildList {
                    val sysMin = setting.systolicMin ?: setting.min
                    val sysMax = setting.systolicMax ?: setting.max
                    if (sysMin != null && systolic < sysMin) add("THRESHOLD_SYS_LOW")
                    if (sysMax != null && systolic > sysMax) add("THRESHOLD_SYS_HIGH")
                    if (setting.diastolicMin != null && diastolic < setting.diastolicMin) add("THRESHOLD_DIA_LOW")
                    if (setting.diastolicMax != null && diastolic > setting.diastolicMax) add("THRESHOLD_DIA_HIGH")
                }

                val spikeReasonsSingle = spikeReasonsSingle(
                    setting = setting,
                    measurementId = measurementId,
                    systolic = systolic,
                    diastolic = diastolic
                )

                spikeReasonsSingle.map { spikeReasons ->
                    val allReasons = thresholdReasons + spikeReasons
                    val severity = when {
                        spikeReasons.isNotEmpty() -> AlertSeverity.CRITICAL
                        thresholdReasons.isNotEmpty() -> AlertSeverity.ATTENTION
                        else -> null
                    }

                    val draft = severity?.let { resolvedSeverity ->
                        AlertEventDraft(
                            type = MeasurementType.PRESSURE,
                            severity = resolvedSeverity,
                            measurementId = measurementId,
                            reasons = allReasons,
                            bloodPressure = BloodPressureEventData(
                                systolic = systolic,
                                diastolic = diastolic,
                                classification = classification
                            ),
                            channelsPlanned = setting.channels
                        )
                    }

                    DraftCandidate(draft)
                }.flatMapCompletable { candidate ->
                    val draft = candidate.draft
                    if (draft == null) Completable.complete()
                    else alertEventRepository.addEvent(draft).ignoreElement()
                }
            }
    }

    private data class DraftCandidate(
        val draft: AlertEventDraft?
    )

    private fun spikeReasonsSingle(
        setting: AlertSetting,
        measurementId: String,
        systolic: Int,
        diastolic: Int
    ): Single<List<String>> {
        val percent = setting.spikePercent
        val windowCount = setting.windowCount
        val prevLimit = (windowCount - 1).coerceAtLeast(0)
        if (percent <= 0 || windowCount < 2 || prevLimit == 0) return Single.just(emptyList())

        return measurementRepository.getRecentMeasurements(prevLimit, types = listOf(MeasurementType.PRESSURE))
            .map { list -> list.filterNot { it.id == measurementId } }
            .map { previous ->
                val sysValues = previous.mapNotNull { it.systolic }.take(prevLimit) + systolic
                val diaValues = previous.mapNotNull { it.diastolic }.take(prevLimit) + diastolic

                buildList {
                    if (hasSpike(sysValues, percent)) add("SPIKE_SYS")
                    if (hasSpike(diaValues, percent)) add("SPIKE_DIA")
                }
            }
    }

    private fun hasSpike(values: List<Int>, percent: Int): Boolean {
        if (values.size < 2) return false
        val min = values.minOrNull() ?: return false
        val max = values.maxOrNull() ?: return false
        if (min <= 0) return false
        val delta = abs(max - min).toDouble()
        val pct = (delta / min.toDouble()) * 100.0
        return pct >= percent.toDouble()
    }
}
