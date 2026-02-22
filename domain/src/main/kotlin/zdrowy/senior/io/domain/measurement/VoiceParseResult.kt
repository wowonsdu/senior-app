package zdrowy.senior.io.domain.measurement

data class VoiceParseResult(
    val measurements: List<ParsedVoiceMeasurement>,
    val skippedTypes: List<MeasurementType>
)
