package zdrowy.senior.io.ui.reminders

import android.content.Context
import android.util.Base64

class MedicationReminderAlarmCache(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(role: ReminderRole, specs: Collection<MedicationReminderSpec>) {
        val encoded = specs.map { encodeSpec(it) }
        prefs.edit().putStringSet(keyFor(role), encoded.toSet()).apply()
    }

    fun load(role: ReminderRole): List<MedicationReminderSpec> {
        val raw = prefs.getStringSet(keyFor(role), emptySet()).orEmpty()
        return raw.mapNotNull { decodeSpec(it) }
    }

    fun clear(role: ReminderRole) {
        prefs.edit().remove(keyFor(role)).apply()
    }

    private fun keyFor(role: ReminderRole): String = "specs_${role.name.lowercase()}"

    private fun encodeSpec(spec: MedicationReminderSpec): String {
        val parts = listOf(
            spec.role.name,
            spec.patientUid,
            spec.patientName,
            spec.medicationId,
            spec.medicationName,
            spec.dosage,
            spec.scheduleTime
        )
        return parts.joinToString("|") { encode(it) }
    }

    private fun decodeSpec(raw: String): MedicationReminderSpec? {
        val parts = raw.split("|")
        if (parts.size != 7) return null
        val role = parts[0].decodeBase64() ?: return null
        val roleEnum = runCatching { ReminderRole.valueOf(role) }.getOrNull() ?: return null
        val patientUid = parts[1].decodeBase64() ?: return null
        val patientName = parts[2].decodeBase64() ?: ""
        val medicationId = parts[3].decodeBase64() ?: return null
        val medicationName = parts[4].decodeBase64() ?: return null
        val dosage = parts[5].decodeBase64() ?: ""
        val scheduleTime = parts[6].decodeBase64() ?: return null
        return MedicationReminderSpec(
            role = roleEnum,
            patientUid = patientUid,
            patientName = patientName,
            medicationId = medicationId,
            medicationName = medicationName,
            dosage = dosage,
            scheduleTime = scheduleTime
        )
    }

    private fun encode(value: String): String =
        Base64.encodeToString(value.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)

    private fun String.decodeBase64(): String? {
        return try {
            val bytes = Base64.decode(this, Base64.NO_WRAP)
            String(bytes, Charsets.UTF_8)
        } catch (error: Throwable) {
            null
        }
    }

    private companion object {
        private const val PREFS_NAME = "medication_reminder_cache"
    }
}
