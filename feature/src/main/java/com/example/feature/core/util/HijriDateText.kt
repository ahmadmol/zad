package com.example.feature.core.util

import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.core.preferences.UserPreferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

/** Refreshes the shared civil date and system zone while visible, including returning from background. */
@Composable
fun currentHijriDateText(): String {
    val preferences: UserPreferences = koinInject()
    val flow = remember(preferences) { preferences.hijriDayOffset.map { it as Int? }.catch { e ->
        if (e is CancellationException) throw e
        emit(null)
    } }
    val offset by flow.collectAsStateWithLifecycle(initialValue = null)
    val provider = remember { HijriDateProvider() }
    val civil = currentCivilDate()
    if (offset == null) return "التاريخ الهجري غير متاح حاليًا"
    return remember(civil, offset) { runCatching { HijriDateProvider.format(provider.date(civil, offset ?: 0)) }
        .getOrElse { "التاريخ الهجري غير متاح • $civil" } }
}

/** Civil date for paired Gregorian/Hijri headers; independent from the prayer location zone. */
@Composable
fun currentCivilDate(): java.time.LocalDate {
    val provider = remember { HijriDateProvider() }
    val date by produceState(initialValue = provider.today()) {
        while (true) { value=provider.today(); delay(1_000) }
    }
    return date
}
