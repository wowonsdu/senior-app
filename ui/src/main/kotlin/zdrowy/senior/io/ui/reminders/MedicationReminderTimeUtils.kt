package zdrowy.senior.io.ui.reminders

import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

internal object MedicationReminderTimeUtils {
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val eventDateFormatter = DateTimeFormatter.BASIC_ISO_DATE
    private val eventTimeFormatter = DateTimeFormatter.ofPattern("HHmm")

    fun parseTimes(schedule: String): List<String> {
        if (schedule.isBlank()) return emptyList()
        return schedule.split(",")
            .map { it.trim() }
            .filter { it.contains(":") && it.length >= 4 }
            .distinct()
    }

    fun nextTriggerMillis(scheduleTime: String, nowMillis: Long): Long? {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
        val time = parseTime(scheduleTime) ?: return null
        val today = now.toLocalDate()
        var candidate = ZonedDateTime.of(today, time, zone)
        if (!candidate.isAfter(now)) {
            candidate = candidate.plusDays(1)
        }
        return candidate.toInstant().toEpochMilli()
    }

    fun buildEventId(medicationId: String, scheduledAtMillis: Long): String {
        val zone = ZoneId.systemDefault()
        val dateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(scheduledAtMillis), zone)
        val date = dateTime.toLocalDate().format(eventDateFormatter)
        val time = dateTime.toLocalTime().format(eventTimeFormatter)
        return "${medicationId}_${date}_${time}"
    }

    private fun parseTime(value: String): LocalTime? {
        return try {
            LocalTime.parse(value.trim(), timeFormatter)
        } catch (error: Throwable) {
            null
        }
    }
}
