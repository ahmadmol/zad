package com.example.mol.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import com.example.mol.MainActivity
import com.example.mol.R
import com.example.feature.prayer.domain.calculator.PrayerCalculator
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.repository.PrayerLocationRepository
import com.example.feature.prayer.domain.repository.PrayerSettingsRepository
import com.example.feature.prayer.widget.PrayerWidgetState
import com.example.feature.prayer.widget.PrayerWidgetStateFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.time.LocalDate
import java.time.ZoneId

/**
 * Phase 7 — Prayer home widget.
 *
 * Reads the canonical prayer domain (`PrayerCalculator` + `PrayerLocationRepository` +
 * `PrayerSettingsRepository`) — the same components the Prayer screen uses. It contains
 * **no second calculation engine**, requests **no location itself** (it only reads the
 * repository's last known state), performs **no network I/O**, and does **not poll**:
 * it schedules exactly one alarm for the next prayer instant and otherwise redraws only
 * when the system asks it to, or when time/date/timezone changes.
 */
class PrayerWidgetProvider : AppWidgetProvider(), KoinComponent {

    private val calculator: PrayerCalculator by inject()
    private val locationRepository: PrayerLocationRepository by inject()
    private val settingsRepository: PrayerSettingsRepository by inject()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        refresh(context, appWidgetManager, appWidgetIds)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_REFRESH,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val manager = AppWidgetManager.getInstance(context)
                val ids = manager.getAppWidgetIds(
                    ComponentName(context, PrayerWidgetProvider::class.java)
                )
                if (ids.isNotEmpty()) refresh(context, manager, ids)
            }
        }
    }

    override fun onDisabled(context: Context) {
        // Last widget removed: stop the refresh alarm so nothing keeps waking up.
        cancelRefreshAlarm(context)
        super.onDisabled(context)
    }

    private fun refresh(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pending = goAsyncSafely()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        scope.launch {
            try {
                val state = buildState(context)
                val views = render(context, state)
                appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
                scheduleNextRefresh(context, state)
            } catch (t: Throwable) {
                // Never let a widget update crash the host launcher.
                val views = render(
                    context,
                    PrayerWidgetState.Unavailable(PrayerWidgetState.Unavailable.Reason.NOT_READY)
                )
                appWidgetIds.forEach { appWidgetManager.updateAppWidget(it, views) }
            } finally {
                pending?.finish()
                scope.cancel()
            }
        }
    }

    private fun goAsyncSafely(): PendingResult? = runCatching { goAsync() }.getOrNull()

    private suspend fun buildState(context: Context): PrayerWidgetState {
        val locationState = locationRepository.observeLocation().first()
        if (locationState !is PrayerLocationState.Available) {
            return PrayerWidgetState.Unavailable(
                PrayerWidgetState.Unavailable.Reason.NO_LOCATION
            )
        }

        val settings = settingsRepository.observeSettings().first()
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val date = LocalDate.now(zone)

        val today = calculator.calculate(date, locationState.location, settings, zone.id, now)
        val tomorrow =
            calculator.calculate(date.plusDays(1), locationState.location, settings, zone.id, now)

        return PrayerWidgetStateFactory.create(
            today = today,
            tomorrow = tomorrow,
            nowEpochMillis = now,
            locationLabel = locationState.location.displayName,
            zoneId = zone
        )
    }

    private fun render(context: Context, state: PrayerWidgetState): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_prayer)

        when (state) {
            is PrayerWidgetState.Unavailable -> {
                views.setTextViewText(
                    R.id.widget_next_prayer,
                    context.getString(R.string.widget_prayer_unavailable_title)
                )
                views.setTextViewText(
                    R.id.widget_next_time,
                    when (state.reason) {
                        PrayerWidgetState.Unavailable.Reason.NO_LOCATION ->
                            context.getString(R.string.widget_prayer_no_location)
                        PrayerWidgetState.Unavailable.Reason.NOT_READY ->
                            context.getString(R.string.widget_prayer_not_ready)
                    }
                )
                views.setTextViewText(R.id.widget_countdown, "")
                views.setTextViewText(R.id.widget_location, "")
            }

            is PrayerWidgetState.Ready -> {
                views.setTextViewText(R.id.widget_next_prayer, state.nextPrayerName.arabic)
                views.setTextViewText(R.id.widget_next_time, state.nextPrayerTimeLabel)
                views.setTextViewText(
                    R.id.widget_countdown,
                    formatCountdown(context, state.minutesUntilNext)
                )
                views.setTextViewText(R.id.widget_location, state.locationLabel.orEmpty())
            }
        }

        views.setOnClickPendingIntent(R.id.widget_root, openPrayerScreenIntent(context))
        return views
    }

    private fun formatCountdown(context: Context, minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            context.getString(R.string.widget_prayer_remaining_hm, hours, mins)
        } else {
            context.getString(R.string.widget_prayer_remaining_m, mins)
        }
    }

    /** Tapping anywhere opens the Prayer screen. */
    private fun openPrayerScreenIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_DESTINATION, DESTINATION_PRAYER)
        }
        return PendingIntent.getActivity(
            context,
            REQUEST_OPEN_PRAYER,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    /**
     * Schedules a single inexact alarm at the next prayer instant.
     *
     * Inexact on purpose: the widget is informational, so it must not consume the
     * app's exact-alarm budget, and a redraw a few minutes late is harmless.
     */
    private fun scheduleNextRefresh(context: Context, state: PrayerWidgetState) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val ready = state as? PrayerWidgetState.Ready ?: return

        val triggerAt = System.currentTimeMillis() +
            (ready.minutesUntilNext * 60_000L).coerceAtLeast(60_000L)

        runCatching {
            alarmManager.set(AlarmManager.RTC, triggerAt, refreshPendingIntent(context))
        }
    }

    private fun cancelRefreshAlarm(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        runCatching { alarmManager.cancel(refreshPendingIntent(context)) }
    }

    private fun refreshPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PrayerWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_REFRESH,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    companion object {
        const val ACTION_REFRESH = "com.example.mol.widget.ACTION_PRAYER_WIDGET_REFRESH"
        const val EXTRA_OPEN_DESTINATION = "open_destination"
        const val DESTINATION_PRAYER = "prayer"

        private const val REQUEST_OPEN_PRAYER = 9101
        private const val REQUEST_REFRESH = 9102

        /** Asks every placed widget to redraw. Safe to call when none exist. */
        fun requestRefresh(context: Context) {
            val intent = Intent(context, PrayerWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
}
