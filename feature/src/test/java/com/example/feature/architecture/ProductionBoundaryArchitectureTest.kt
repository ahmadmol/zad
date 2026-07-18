package com.example.feature.architecture

import com.example.feature.ihsanplus.integration.di.IhsanPlusDiSpec
import com.example.feature.ihsanplus.integration.routing.IhsanPlusRouteSpec
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Lightweight source/package architecture gates for Part 5.
 */
class ProductionBoundaryArchitectureTest {

    private val repoRoot: File = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    @Test
    fun `no ihsan plus route appears in Screen or AppNavHost`() {
        val screen = read("app/src/main/java/com/example/mol/navigation/Screen.kt")
        val nav = read("app/src/main/java/com/example/mol/navigation/AppNavHost.kt")
        IhsanPlusRouteSpec.ALL.forEach { route ->
            assertFalse(screen.contains(route.route))
            assertFalse(nav.contains(route.route))
        }
        assertFalse(screen.contains("ihsan_plus", ignoreCase = true))
        assertFalse(nav.contains("ihsanPlus", ignoreCase = true))
        assertFalse(nav.contains("IhsanPlusDaily"))
        assertFalse(nav.contains("IhsanPlusPrayerAssist"))
        assertFalse(nav.contains("IhsanPlusCharityTrust"))
        assertFalse(screen.contains("inbox_screen"))
        assertTrue(screen.contains("tasbih_screen"))
        assertTrue(screen.contains("ihsan_details"))
    }

    @Test
    fun `production appModule does not include ihsan plus fake modules`() {
        val appModule = read("app/src/main/java/com/example/mol/di/AppModule.kt")
        IhsanPlusDiSpec.forbiddenReleaseModuleNames.forEach { name ->
            assertFalse("appModule must not include $name", appModule.contains(name))
        }
        assertFalse(appModule.contains("ihsanplus", ignoreCase = true))
        assertFalse(appModule.contains("DemoIhsanPlus"))
    }

    @Test
    fun `demo data sources are clearly named Demo`() {
        val daily = File(repoRoot, "feature/src/main/java/com/example/feature/ihsanplus/daily/data")
        val charity = File(repoRoot, "feature/src/main/java/com/example/feature/ihsanplus/charitytrust/data")
        val prayer = File(repoRoot, "feature/src/main/java/com/example/feature/ihsanplus/prayerassist/data")
        assertTrue(File(daily, "DemoIhsanPlusDailyDataSource.kt").exists())
        assertTrue(File(daily, "DemoIhsanPlusDailyRepository.kt").exists())
        assertTrue(File(charity, "DemoIhsanPlusCharityTrustDataSource.kt").exists())
        assertTrue(File(charity, "DemoIhsanPlusCharityTrustRepository.kt").exists())
        assertTrue(File(prayer, "DemoIhsanPlusPrayerAssistDataSource.kt").exists())
        assertFalse(File(daily, "IhsanPlusDailyFakeDataSource.kt").exists())
        assertFalse(File(daily, "IhsanPlusDailyRepositoryImpl.kt").exists())
    }

    @Test
    fun `home viewmodel does not import location or scheduler classes`() {
        val vm = read(
            "feature/src/main/java/com/example/feature/dashboard/presentation/HomeDashboardViewModel.kt"
        )
        assertFalse(vm.contains("AlarmManager"))
        assertFalse(vm.contains("LocationManager"))
        assertFalse(vm.contains("PrayerNotificationScheduler"))
        assertFalse(vm.contains("FusedLocation"))
    }

    @Test
    fun `settings and statistics viewmodels do not import azkar viewmodel`() {
        val settings = read(
            "feature/src/main/java/com/example/feature/settings/presentation/SettingsViewModel.kt"
        )
        val stats = read(
            "feature/src/main/java/com/example/feature/statistics/presentation/StatisticsViewModel.kt"
        )
        assertFalse(settings.contains("AzkarViewModel"))
        assertFalse(settings.contains("AlarmManager"))
        assertFalse(stats.contains("AzkarViewModel"))
    }

    @Test
    fun `production prayer adapters do not import demo data sources`() {
        val adaptersDir = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ihsanplus/integration/adapter"
        )
        adaptersDir.listFiles()?.forEach { file ->
            val text = file.readText()
            assertFalse(text.contains("DemoIhsanPlus"))
            assertFalse(text.contains("FakeDataSource"))
            assertFalse(text.contains("CharityCapability.BackendVerified"))
            assertFalse(text.contains("IhsanPlusVerificationStatus"))
        }
    }

    @Test
    fun `database module does not enable destructive migration fallback`() {
        val db = read("app/src/main/java/com/example/mol/di/DatabaseModule.kt")
        assertFalse(db.contains("fallbackToDestructiveMigration"))
        assertTrue(db.contains("IhsanDatabaseMigrations"))
    }

    private fun read(relativePath: String): String =
        File(repoRoot, relativePath).readText()
}
