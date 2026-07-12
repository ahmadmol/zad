package com.example.feature.ihsanplus.prayerassist.domain.model

data class IhsanPlusPrayerAssistDashboard(
    val locationLabel: String,
    val calculationMethodLabel: String,
    val madhabLabel: String,
    val nextPrayer: IhsanPlusNextPrayerInfo,
    val notificationPreferences: IhsanPlusPrayerNotificationPreferences,
    val reminderPreviewItems: List<IhsanPlusPrayerReminderPreviewItem>,
    val qiblaAssist: IhsanPlusQiblaAssist,
    val calibrationTips: List<IhsanPlusCompassCalibrationTip>,
    val quickActions: List<IhsanPlusPrayerAssistQuickAction>
)

data class IhsanPlusNextPrayerInfo(
    val prayerName: String,
    val prayerTimeLabel: String,
    val remainingTimeLabel: String,
    val isCurrentDay: Boolean
)

data class IhsanPlusPrayerNotificationPreferences(
    val adhanEnabled: Boolean,
    val prePrayerReminderEnabled: Boolean,
    val prePrayerReminderMinutes: Int,
    val iqamaReminderEnabled: Boolean,
    val iqamaReminderMinutes: Int,
    val silentModeLabel: String
)

data class IhsanPlusPrayerReminderPreviewItem(
    val id: String,
    val prayerName: String,
    val prayerTimeLabel: String,
    val adhanNotificationLabel: String,
    val prePrayerReminderLabel: String,
    val iqamaReminderLabel: String
)

data class IhsanPlusQiblaAssist(
    val qiblaDirectionLabel: String,
    val compassAccuracyLabel: String,
    val deviceSupportLabel: String,
    val offlineStatusLabel: String,
    val lastKnownLocationLabel: String
)

data class IhsanPlusCompassCalibrationTip(
    val id: String,
    val title: String,
    val description: String
)

data class IhsanPlusPrayerAssistQuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: IhsanPlusPrayerAssistQuickActionType
)

enum class IhsanPlusPrayerAssistQuickActionType {
    OPEN_PRAYER_SETTINGS,
    OPEN_QIBLA,
    ENABLE_NOTIFICATIONS,
    CHANGE_CITY,
    CALIBRATE_COMPASS
}
