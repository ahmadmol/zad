package com.example.mol.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.RemoteViews
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.clock.PrayerClock
import com.example.feature.prayer.domain.model.*
import com.example.feature.prayer.widget.*
import com.example.mol.MainActivity
import com.example.mol.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.context.GlobalContext

const val WIDGET_OPEN_PRAYER = "com.example.mol.widget.OPEN_PRAYER"
const val WIDGET_CONTINUE_QURAN = "com.example.mol.widget.CONTINUE_QURAN"
private const val WIDGET_REFRESH = "com.example.mol.widget.REFRESH"
data class WidgetOpenRequest(val sequence: Long, val action: String)

/** Only the explicit widget receiver can deliver update requests; no network or history access. */
class PrayerWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) { refresh() }
    override fun onAppWidgetOptionsChanged(context: Context, manager: AppWidgetManager, id: Int, options: Bundle) { refresh() }
    override fun onDeleted(context: Context, ids: IntArray) { coordinator().stopIfUnused() }
    override fun onDisabled(context: Context) { coordinator().stopIfUnused() }
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action in setOf(WIDGET_REFRESH, ACTION_REFRESH, Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_MY_PACKAGE_REPLACED,
                Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, Intent.ACTION_DATE_CHANGED, Intent.ACTION_LOCALE_CHANGED)) {
            refresh()
        }
    }
    private fun refresh() {
        val pending = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        scope.launch {
            try { coordinator().start(); coordinator().refreshOnce() }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { android.util.Log.w("PrayerWidget", "widget_refresh_unavailable") }
            finally { pending.finish(); scope.cancel() }
        }
    }
    private fun coordinator() = GlobalContext.get().get<PrayerWidgetCoordinator>()
    companion object {
        const val ACTION_REFRESH = "com.example.mol.widget.ACTION_PRAYER_WIDGET_REFRESH"
        const val EXTRA_OPEN_DESTINATION = "open_destination"
        const val DESTINATION_PRAYER = "prayer"
        fun requestRefresh(context: Context) { context.sendBroadcast(Intent(context, PrayerWidgetProvider::class.java).setAction(ACTION_REFRESH)) }
    }
}

class PrayerWidgetCoordinator(private val context: Context, private val facade: PrayerTimesFacade,
    private val clock: PrayerClock) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var job: Job? = null
    private val manager get() = AppWidgetManager.getInstance(context)
    private fun ids() = manager.getAppWidgetIds(ComponentName(context, PrayerWidgetProvider::class.java))
    private data class Snapshot(val day: PrayerDay?, val next: NextPrayer?, val location: PrayerLocationState)
    @Synchronized fun start(force: Boolean = false) {
        if (ids().isEmpty()) return
        if (job?.isActive != true) {
            job = scope.launch {
                combine(facade.prayerDay, facade.nextPrayer, facade.locationState) { day, next, location ->
                    Snapshot(day, next?.copy(remainingMillis = 0), location)
                }.distinctUntilChanged().collect { safeRender(it) }
            }
        }
        if (force) scope.launch { safeRender(Snapshot(facade.prayerDay.value, facade.nextPrayer.value, facade.locationState.value)) }
    }
    suspend fun refreshOnce() {
        if (ids().isEmpty()) return
        val snapshot = withTimeoutOrNull(6000) {
            combine(facade.prayerDay, facade.nextPrayer, facade.locationState) { day, next, location -> Snapshot(day, next, location) }
                .first { it.location !is PrayerLocationState.Loading && (it.location !is PrayerLocationState.Available ||
                    PrayerWidgetStateFactory.fromFacade(it.day, it.next, it.location, clock.nowEpochMillis(), clock.zoneId()) is PrayerWidgetState.Ready) }
        } ?: Snapshot(facade.prayerDay.value, facade.nextPrayer.value, facade.locationState.value)
        safeRender(snapshot)
    }
    @Synchronized fun stopIfUnused() {
        if (ids().isNotEmpty()) return
        job?.cancel(); job = null
        context.getSystemService(AlarmManager::class.java).cancel(refreshIntent())
    }
    private fun refreshIntent() = PendingIntent.getBroadcast(context, 4120,
        Intent(context, PrayerWidgetProvider::class.java).setAction(WIDGET_REFRESH), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private suspend fun safeRender(snapshot: Snapshot) {
        try { render(snapshot) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { android.util.Log.w("PrayerWidget", "widget_update_unavailable") }
    }
    private suspend fun render(snapshot: Snapshot) = withContext(Dispatchers.Main) {
        if (ids().isEmpty()) return@withContext
        val now = clock.nowEpochMillis()
        val state = PrayerWidgetStateFactory.fromFacade(snapshot.day, snapshot.next, snapshot.location, now, clock.zoneId())
        val views = RemoteViews(context.packageName, R.layout.widget_prayer)
        views.setOnClickPendingIntent(R.id.widget_prayer, openIntent(WIDGET_OPEN_PRAYER, 4121))
        views.setOnClickPendingIntent(R.id.widget_quran, openIntent(WIDGET_CONTINUE_QURAN, 4122))
        if (state is PrayerWidgetState.Ready && snapshot.next != null) {
            views.setTextViewText(R.id.widget_next_prayer, context.getString(if (state.nextPrayerName == PrayerName.SUNRISE) R.string.widget_next_event else R.string.widget_next_prayer, state.nextPrayerName.arabic))
            views.setTextViewText(R.id.widget_next_time, state.nextPrayerTimeLabel)
            views.setViewVisibility(R.id.widget_countdown, View.VISIBLE)
            views.setChronometer(R.id.widget_countdown, SystemClock.elapsedRealtime() + (snapshot.next.epochMillis - now), null, true)
            views.setChronometerCountDown(R.id.widget_countdown, true)
            views.setTextViewText(R.id.widget_location, context.getString(R.string.widget_updated_at,
                java.time.format.DateTimeFormatter.ofPattern("HH:mm", java.util.Locale.forLanguageTag("ar")).format(java.time.Instant.ofEpochMilli(now).atZone(clock.zoneId()))))
        } else {
            views.setTextViewText(R.id.widget_next_prayer, context.getString(R.string.widget_prayer_unavailable_title))
            views.setTextViewText(R.id.widget_next_time, context.getString(if ((state as PrayerWidgetState.Unavailable).reason == PrayerWidgetState.Unavailable.Reason.NO_LOCATION) R.string.widget_prayer_no_location else R.string.widget_prayer_not_ready))
            views.setViewVisibility(R.id.widget_countdown, View.GONE)
            views.setChronometer(R.id.widget_countdown, 0, null, false)
            views.setTextViewText(R.id.widget_location, context.getString(R.string.widget_open_to_setup))
        }
        manager.updateAppWidget(ids(), views)
        // Separate inexact, prayer-boundary/midnight alarm; does not touch the adhan alarm ledger.
        context.getSystemService(AlarmManager::class.java).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            PrayerWidgetStateFactory.refreshAt(now, snapshot.next, clock.zoneId()), refreshIntent())
    }
    private fun openIntent(action: String, code: Int) = PendingIntent.getActivity(context, code,
        Intent(context, MainActivity::class.java).setAction(action).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
