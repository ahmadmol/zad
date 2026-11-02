package com.example.feature.prayer.presentation

import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.domain.model.PrayerCalculationMethod
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine

internal data class PrayerSheetState(
    val method: String = "MUSLIM_WORLD_LEAGUE",
    val madhab: String = "SHAFI",
    val autoLocation: Boolean = true,
    val cityName: String = "",
    val soundType: String = "DEFAULT_ATHAN",
    val prePrayerMins: Int = 0,
    val iqamahMins: Int = 0,
    val ready: Boolean = false,
    val error: String? = null
)

internal val prayerMethodChoices = listOf(
    PrayerCalculationMethod.MUSLIM_WORLD_LEAGUE to "رابطة العالم الإسلامي",
    PrayerCalculationMethod.UMM_AL_QURA to "أم القرى (مكة)",
    PrayerCalculationMethod.EGYPTIAN to "الهيئة المصرية العامة للمساحة",
    PrayerCalculationMethod.KARACHI to "جامعة العلوم الإسلامية بكراتشي",
    PrayerCalculationMethod.NORTH_AMERICA to "ISNA (أمريكا الشمالية)",
    PrayerCalculationMethod.DUBAI to "دبي",
    PrayerCalculationMethod.KUWAIT to "الكويت",
    PrayerCalculationMethod.QATAR to "قطر"
)

/** Defaults are never offered as loaded user settings after a failed read. */
internal fun SettingsManager.observePrayerSheetState(): Flow<PrayerSheetState> = combine(
    combine(calculationMethodFlow,madhabFlow,useAutoLocationFlow,manualLocationCityFlow,notificationSoundTypeFlow) {
        method,madhab,auto,city,sound -> PrayerSheetState(method,madhab,auto,city,sound)
    },
    prePrayerNotificationMinutesFlow,iqamahNotificationMinutesFlow
) { state,pre,iqamah -> state.copy(prePrayerMins=pre,iqamahMins=iqamah,ready=true) }
    .catch { error ->
        if (error is CancellationException) throw error
        emit(PrayerSheetState(error="تعذر قراءة إعدادات الصلاة؛ أعد المحاولة دون تغييرها"))
    }
