package com.example.mol.ui

import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import com.example.feature.prayer.domain.model.PrayerLocationState
import java.io.File
import java.time.ZoneId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.GlobalContext

/** Changes only the dedicated QA emulator's timezone and restores it, without restarting the app. */
class AuditTimezoneChangeTest {
    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private fun shell(command: String) = android.os.ParcelFileDescriptor.AutoCloseInputStream(
        inst.uiAutomation.executeShellCommand(command)).bufferedReader().use { it.readText().trim() }

    @Test fun existingPrayerFacadeRecalculatesAfterRealAndroidTimezoneChange() = runBlocking {
        check(inst.targetContext.packageName == "com.example.mol.referenceqa")
        val original = shell("getprop persist.sys.timezone")
        require(original.isNotBlank())
        val auto = shell("settings get global auto_time_zone")
        val facade = GlobalContext.get().get<PrayerTimesFacade>()
        withTimeout(30_000) { while (facade.locationState.value !is PrayerLocationState.Available) delay(200) }
        val observations = mutableListOf<String>()
        try {
            shell("settings put global auto_time_zone 0")
            for (zone in listOf("UTC", "Asia/Tokyo")) {
                shell("cmd alarm set-timezone $zone")
                withTimeout(30_000) {
                    while (ZoneId.systemDefault().id != zone || facade.prayerDay.value?.timeZoneId != zone) delay(200)
                }
                val day = checkNotNull(facade.prayerDay.value)
                assertEquals(java.time.LocalDate.now(ZoneId.of(zone)).toEpochDay(), day.dateEpochDay)
                assertEquals(6, day.instants.size)
                assertTrue(day.instants.zipWithNext().all { (a, b) -> a.epochMillis < b.epochMillis })
                observations += "PASS zone=$zone dateEpochDay=${day.dateEpochDay} prayers=${day.instants.size} next=${facade.nextPrayer.value?.name}"
            }
            File(inst.targetContext.getExternalFilesDir(null), "audit-timezone-change.txt")
                .writeText(observations.joinToString("\n") + "\nSame app process and facade; actual Android timezone broadcast; original=$original\n")
        } finally {
            shell("cmd alarm set-timezone $original")
            if (auto == "null") shell("settings delete global auto_time_zone")
            else shell("settings put global auto_time_zone $auto")
            withTimeout(30_000) { while (facade.prayerDay.value?.timeZoneId != original) delay(200) }
        }
    }
}
