package zdrowy.senior.io.domain.measurement

import kotlin.math.max

class ClassifyBloodPressureUseCase {
    operator fun invoke(
        systolic: Int,
        diastolic: Int,
        standard: BloodPressureStandard = BloodPressureStandard.ESC_ESH_OFFICE
    ): BloodPressureClassification {
        return when (standard) {
            BloodPressureStandard.ESC_ESH_OFFICE -> classifyEscEshOffice(systolic, diastolic)
        }
    }

    private fun classifyEscEshOffice(
        systolic: Int,
        diastolic: Int
    ): BloodPressureClassification {
        val systolicSeverity = when {
            systolic < 120 -> BloodPressureSeverity.OPTIMAL
            systolic <= 129 -> BloodPressureSeverity.NORMAL
            systolic <= 139 -> BloodPressureSeverity.HIGH_NORMAL
            systolic <= 159 -> BloodPressureSeverity.HTN1
            systolic <= 179 -> BloodPressureSeverity.HTN2
            else -> BloodPressureSeverity.HTN3
        }
        val diastolicSeverity = when {
            diastolic < 80 -> BloodPressureSeverity.OPTIMAL
            diastolic <= 84 -> BloodPressureSeverity.NORMAL
            diastolic <= 89 -> BloodPressureSeverity.HIGH_NORMAL
            diastolic <= 99 -> BloodPressureSeverity.HTN1
            diastolic <= 109 -> BloodPressureSeverity.HTN2
            else -> BloodPressureSeverity.HTN3
        }
        val severity = max(systolicSeverity.ordinal, diastolicSeverity.ordinal)
            .let { BloodPressureSeverity.values()[it] }
        val isolatedSystolic = systolic >= 140 && diastolic < 90
        return BloodPressureClassification(
            standard = BloodPressureStandard.ESC_ESH_OFFICE,
            severity = severity,
            isolatedSystolic = isolatedSystolic
        )
    }
}

