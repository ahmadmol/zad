package com.example.mol.architecture

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class ReleaseMinifyConfigurationTest {

    private val repoRoot: File = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .first { File(it, "settings.gradle.kts").exists() }
    }

    @Test
    fun `release minify and resource shrinking are enabled`() {
        val gradle = File(repoRoot, "app/build.gradle.kts").readText()
        assertTrue(gradle.contains("isMinifyEnabled = true"))
        assertTrue(gradle.contains("isShrinkResources = true"))
        assertFalse(
            "broad keep-all rule must not ship",
            File(repoRoot, "app/proguard-rules.pro").readText()
                .contains("-keep class ** { *; }")
        )
    }

    @Test
    fun `gitignore excludes keystore artifacts`() {
        val ignore = File(repoRoot, ".gitignore").readText()
        assertTrue(ignore.contains("*.jks"))
        assertTrue(ignore.contains("*.keystore"))
        assertTrue(ignore.contains("local.properties"))
    }
}
