package com.example.mol.architecture

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Phase 10 — proves no IhsanPlus Demo/Fake data source is reachable in a release build.
 *
 * The guard is structural rather than reflective: it follows the actual Koin wiring
 * from `appModule` outward and asserts that every module reachable from it binds only
 * `Production*` adapters. A reflective check would need the Android runtime; this runs
 * as a plain unit test in CI and fails the moment someone re-includes a demo module.
 */
class IhsanPlusReleaseGraphTest {

    private val repoRoot: File = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    private val appModule: String
        get() = File(repoRoot, "app/src/main/java/com/example/mol/di/AppModule.kt").readText()

    private val productionModule: String
        get() = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ihsanplus/integration/di/" +
                "IhsanPlusProductionModule.kt"
        ).readText()

    /** Demo/Fake source class names that must never appear in the release graph. */
    private val forbiddenSources = listOf(
        "DemoIhsanPlusDailyDataSource",
        "DemoIhsanPlusDailyRepository",
        "DemoIhsanPlusCharityTrustDataSource",
        "DemoIhsanPlusCharityTrustRepository",
        "DemoIhsanPlusPrayerAssistDataSource"
    )

    @Test
    fun `app module does not include any demo backed ihsan plus module`() {
        listOf("ihsanPlusDailyModule", "ihsanPlusCharityTrustModule").forEach { module ->
            assertFalse(
                module + " binds demo sources and must not be in appModule",
                appModule.contains(module)
            )
        }
    }

    @Test
    fun `app module includes only the production ihsan plus module`() {
        assertTrue(appModule.contains("ihsanPlusProductionModule"))
    }

    @Test
    fun `the production module binds no demo or fake source`() {
        forbiddenSources.forEach { forbidden ->
            assertFalse(
                "ihsanPlusProductionModule must not reference " + forbidden,
                productionModule.contains(forbidden)
            )
        }
        assertFalse(productionModule.contains("Fake"))
    }

    @Test
    fun `the production module binds production adapters`() {
        assertTrue(productionModule.contains("Production"))
    }

    @Test
    fun `no demo source is referenced anywhere in the app module source set`() {
        val appSources = File(repoRoot, "app/src/main/java")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .toList()

        appSources.forEach { file ->
            val text = file.readText()
            forbiddenSources.forEach { forbidden ->
                assertFalse(
                    file.name + " must not reference " + forbidden,
                    text.contains(forbidden)
                )
            }
        }
    }

    @Test
    fun `demo modules are only referenced by their own package or tests`() {
        // A demo module may exist in :feature (it is used by previews and by its own
        // tests) but must not be wired from anywhere that the release graph reaches.
        val leaks = File(repoRoot, "feature/src/main/java")
            .walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filterNot { it.path.replace('\\', '/').contains("/ihsanplus/") }
            .filter { file ->
                val text = file.readText()
                forbiddenSources.any { text.contains(it) }
            }
            .map { it.name }
            .toList()

        assertTrue(
            "demo sources leaked outside the ihsanplus package: " + leaks,
            leaks.isEmpty()
        )
    }

    @Test
    fun `charity trust remains disabled without a real provider`() {
        val policy = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ihsanplus/integration/policy/" +
                "IhsanPlusReleasePolicy.kt"
        )
        assertTrue(policy.exists())
        val text = policy.readText()
        // Demo data and unbacked "verified" claims must both be refused in production.
        assertTrue(text.contains("CharityCapability.Demo -> false"))
        assertTrue(text.contains("CharityCapability.BackendVerified -> false"))
    }

    @Test
    fun `the forbidden module list is kept in sync with the spec`() {
        val spec = File(
            repoRoot,
            "feature/src/main/java/com/example/feature/ihsanplus/integration/di/" +
                "IhsanPlusDiSpec.kt"
        ).readText()
        assertTrue(spec.contains("ihsanPlusDailyModule"))
        assertTrue(spec.contains("ihsanPlusCharityTrustModule"))
    }
}
