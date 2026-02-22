package zdrowy.senior.io.domain.measurement

import java.text.Normalizer

class ParseVoiceMeasurementsUseCase {
    private val keywordToType = mapOf(
        "cukier" to MeasurementType.SUGAR,
        "glukoza" to MeasurementType.SUGAR,
        "insulina" to MeasurementType.INSULIN,
        "puls" to MeasurementType.PULSE,
        "tetno" to MeasurementType.PULSE,
        "cisnienie" to MeasurementType.PRESSURE
    )
    private val keywordRegex = Regex(
        "\\b(${keywordToType.keys.joinToString("|")})\\b"
    )
    private val numberRegex = Regex("\\d+(?:[\\.,]\\d+)?")

    operator fun invoke(text: String): VoiceParseResult {
        val normalized = normalize(text)
        val matches = keywordRegex.findAll(normalized)
            .map { match ->
                KeywordMatch(
                    index = match.range.first,
                    keyword = match.value,
                    type = keywordToType.getValue(match.value)
                )
            }
            .sortedBy { it.index }
            .toList()

        if (matches.isEmpty()) {
            return VoiceParseResult(emptyList(), emptyList())
        }

        val measurements = mutableListOf<ParsedVoiceMeasurement>()
        val skipped = mutableListOf<MeasurementType>()

        for (i in matches.indices) {
            val current = matches[i]
            val start = (current.index + current.keyword.length).coerceAtMost(normalized.length)
            val end = if (i + 1 < matches.size) matches[i + 1].index else normalized.length
            val segment = normalized.substring(start, end)
            val numbers = numberRegex.findAll(segment)
                .map { it.value.replace(',', '.') }
                .toList()

            if (current.type == MeasurementType.PRESSURE) {
                val systolic = numbers.getOrNull(0)?.toDoubleOrNull()?.toInt()
                val diastolic = numbers.getOrNull(1)?.toDoubleOrNull()?.toInt()
                if (systolic != null && diastolic != null) {
                    measurements += ParsedVoiceMeasurement(
                        type = MeasurementType.PRESSURE,
                        systolic = systolic,
                        diastolic = diastolic
                    )
                } else {
                    skipped += MeasurementType.PRESSURE
                }
            } else {
                val value = numbers.firstOrNull()?.toDoubleOrNull()
                if (value != null) {
                    measurements += ParsedVoiceMeasurement(
                        type = current.type,
                        value = value
                    )
                } else {
                    skipped += current.type
                }
            }
        }

        return VoiceParseResult(measurements, skipped)
    }

    private fun normalize(text: String): String {
        val lower = text.lowercase()
        val normalized = Normalizer.normalize(lower, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
    }

    private data class KeywordMatch(
        val index: Int,
        val keyword: String,
        val type: MeasurementType
    )
}
