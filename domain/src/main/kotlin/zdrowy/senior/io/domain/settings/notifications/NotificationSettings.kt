package zdrowy.senior.io.domain.settings.notifications

import zdrowy.senior.io.domain.alert.AlertSetting
import zdrowy.senior.io.domain.measurement.MeasurementType

data class NotificationSettings(
    val alerts: List<AlertSetting>
) {
    fun alert(type: MeasurementType): AlertSetting? = alerts.firstOrNull { it.type == type }
}

