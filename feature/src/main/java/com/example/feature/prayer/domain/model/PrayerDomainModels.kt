package com.example.feature.prayer.domain.model

enum class PrayerName(val arabic: String, val english: String) {
    FAJR("الفجر", "Fajr"),
    SUNRISE("الشروق", "Sunrise"),
    DHUHR("الظهر", "Dhuhr"),
    ASR("العصر", "Asr"),
    MAGHRIB("المغرب", "Maghrib"),
    ISHA("العشاء", "Isha");

    val isNotifiable: Boolean get() = this != SUNRISE
}

data class PrayerInstant(
    val name: PrayerName,
    val epochMillis: Long
)

data class PrayerDay(
    val dateEpochDay: Long,
    val timeZoneId: String,
    val location: PrayerLocation,
    val settings: PrayerCalculationSettings,
    val instants: List<PrayerInstant>
) {
    fun instant(name: PrayerName): PrayerInstant? = instants.firstOrNull { it.name == name }
}

data class NextPrayer(
    val name: PrayerName,
    val epochMillis: Long,
    val remainingMillis: Long,
    val previousEpochMillis: Long?
)

data class PrayerLocation(
    val latitude: Double,
    val longitude: Double,
    val displayName: String
)

sealed interface PrayerLocationSource {
    data object Device : PrayerLocationSource
    data object Saved : PrayerLocationSource
    data object Manual : PrayerLocationSource
}

enum class PrayerLocationUnavailableReason {
    PERMISSION_DENIED,
    PROVIDER_DISABLED,
    TIMEOUT,
    NO_SAVED_LOCATION,
    UNKNOWN
}

sealed interface PrayerLocationState {
    data object Loading : PrayerLocationState

    data class Available(
        val location: PrayerLocation,
        val source: PrayerLocationSource
    ) : PrayerLocationState

    data class Unavailable(
        val reason: PrayerLocationUnavailableReason
    ) : PrayerLocationState
}

enum class PrayerCalculationMethod {
    MUSLIM_WORLD_LEAGUE,
    EGYPTIAN,
    KARACHI,
    UMM_AL_QURA,
    DUBAI,
    MOON_SIGHTING_COMMITTEE,
    NORTH_AMERICA,
    KUWAIT,
    QATAR,
    SINGAPORE,
    OTHER
}

enum class PrayerMadhhab {
    SHAFI,
    HANAFI
}

data class PrayerOffsets(
    val fajrMinutes: Int = 0,
    val sunriseMinutes: Int = 0,
    val dhuhrMinutes: Int = 0,
    val asrMinutes: Int = 0,
    val maghribMinutes: Int = 0,
    val ishaMinutes: Int = 0
) {
    fun minutesFor(name: PrayerName): Int = when (name) {
        PrayerName.FAJR -> fajrMinutes
        PrayerName.SUNRISE -> sunriseMinutes
        PrayerName.DHUHR -> dhuhrMinutes
        PrayerName.ASR -> asrMinutes
        PrayerName.MAGHRIB -> maghribMinutes
        PrayerName.ISHA -> ishaMinutes
    }
}

data class PrayerCalculationSettings(
    val method: PrayerCalculationMethod = PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE,
    val madhhab: PrayerMadhhab = PrayerMadhhab.SHAFI,
    val offsets: PrayerOffsets = PrayerOffsets(),
    val useAutoLocation: Boolean = true,
    val prePrayerNotificationMinutes: Int = 0,
    val iqamahNotificationMinutes: Int = 0,
    val notificationsEnabled: Boolean = true
)

data class PrayerSchedulePolicy(
    val includeSunrise: Boolean = true,
    val prePrayerMinutes: Int = 0,
    val iqamahMinutes: Int = 0,
    val endOfPrayerReminderMinutesBeforeNext: Int = 15
)

enum class PrayerAlarmKind {
    PRE_PRAYER,
    EXACT,
    IQAMAH,
    END_REMINDER
}

data class PrayerAlarmRequest(
    val stableId: Int,
    val prayerName: PrayerName,
    val kind: PrayerAlarmKind,
    val triggerEpochMillis: Long,
    val title: String,
    val message: String
)

data class PrayerSchedule(
    val dateEpochDay: Long,
    val timeZoneId: String,
    val location: PrayerLocation,
    val settingsFingerprint: String,
    val policy: PrayerSchedulePolicy,
    val alarms: List<PrayerAlarmRequest>
)

sealed interface PrayerScheduleResult {
    data class Scheduled(
        val scheduledCount: Int,
        val skippedPastCount: Int,
        val usedInexactFallback: Boolean
    ) : PrayerScheduleResult

    data class Skipped(
        val reason: String
    ) : PrayerScheduleResult

    data class Failed(
        val summary: String
    ) : PrayerScheduleResult
}

sealed interface PrayerAlarmPermissionState {
    data object GrantedExact : PrayerAlarmPermissionState
    data object InexactOnly : PrayerAlarmPermissionState
    data object NotificationsDenied : PrayerAlarmPermissionState
    data object Unknown : PrayerAlarmPermissionState
}

data class PrayerSystemStatus(
    val locationState: PrayerLocationState,
    val alarmPermission: PrayerAlarmPermissionState,
    val lastReconciliationEpochMillis: Long?,
    val lastReconciliationSuccess: Boolean?,
    val lastFailureSummary: String?,
    val scheduledAlarmCount: Int
)

enum class PrayerReconciliationReason {
    ApplicationStart,
    SettingsChanged,
    LocationChanged,
    NotificationPermissionChanged,
    ExactAlarmPermissionChanged,
    DeviceBooted,
    TimeChanged,
    TimezoneChanged,
    AppUpdated,
    ManualRetry
}

enum class PrayerOperationalEventType {
    SCHEDULE_SUCCESS,
    SCHEDULE_SKIPPED,
    SCHEDULE_FAILED,
    LOCATION_UNAVAILABLE,
    PERMISSION_CHANGED,
    RECONCILE_STARTED
}

data class PrayerOperationalEvent(
    val type: PrayerOperationalEventType,
    val reason: PrayerReconciliationReason?,
    val summary: String,
    val epochMillis: Long
)
