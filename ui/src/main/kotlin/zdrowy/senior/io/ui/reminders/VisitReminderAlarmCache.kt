package zdrowy.senior.io.ui.reminders

import android.content.Context
import android.util.Base64

class VisitReminderAlarmCache(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(specs: Collection<VisitReminderSpec>) {
        if (specs.isEmpty()) {
            prefs.edit().remove(KEY_SPECS).apply()
            return
        }
        val encoded = specs.map { encodeSpec(it) }.toSet()
        prefs.edit().putStringSet(KEY_SPECS, encoded).apply()
    }

    fun load(): List<VisitReminderSpec> {
        val raw = prefs.getStringSet(KEY_SPECS, emptySet()).orEmpty()
        return raw.mapNotNull { decodeSpec(it) }
    }

    fun clear() {
        prefs.edit().remove(KEY_SPECS).apply()
    }

    private fun encodeSpec(spec: VisitReminderSpec): String {
        val parts = listOf(
            spec.visitId,
            spec.patientUid,
            spec.patientName,
            spec.visitTitle,
            spec.location,
            spec.scheduledAtMs.toString(),
            spec.reminderOffsetMinutes.toString()
        )
        return parts.joinToString("|") { encode(it) }
    }

    private fun decodeSpec(raw: String): VisitReminderSpec? {
        val parts = raw.split("|")
        if (parts.size != 7) return null
        val visitId = parts[0].decodeBase64() ?: return null
        val patientUid = parts[1].decodeBase64() ?: return null
        val patientName = parts[2].decodeBase64().orEmpty()
        val visitTitle = parts[3].decodeBase64().orEmpty()
        val location = parts[4].decodeBase64().orEmpty()
        val scheduledAtMs = parts[5].decodeBase64()?.toLongOrNull() ?: return null
        val reminderOffsetMinutes = parts[6].decodeBase64()?.toIntOrNull() ?: return null

        return VisitReminderSpec(
            visitId = visitId,
            patientUid = patientUid,
            patientName = patientName,
            visitTitle = visitTitle,
            location = location,
            scheduledAtMs = scheduledAtMs,
            reminderOffsetMinutes = reminderOffsetMinutes
        )
    }

    private fun encode(value: String): String {
        return Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
    }

    private fun String.decodeBase64(): String? {
        return try {
            val bytes = Base64.decode(this, Base64.NO_WRAP)
            String(bytes, Charsets.UTF_8)
        } catch (error: Throwable) {
            null
        }
    }

    private companion object {
        private const val PREFS_NAME = "visit_reminder_cache"
        private const val KEY_SPECS = "specs"
    }
}
