package com.example.feature.ui

import com.example.designsystem.component.IhsanHeaderMode
import com.example.designsystem.component.IhsanTopBarStyle
import com.example.designsystem.component.toHeaderMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HeaderAndSplashPolishTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    private fun read(relativePath: String): String =
        File(repoRoot, relativePath).readText()

    @Test
    fun `topBar styles map deterministically to Compact and Expanded modes`() {
        assertEquals(IhsanHeaderMode.Expanded, IhsanTopBarStyle.Hero.toHeaderMode())
        assertEquals(IhsanHeaderMode.Compact, IhsanTopBarStyle.Surface.toHeaderMode())
        assertEquals(IhsanHeaderMode.Compact, IhsanTopBarStyle.CompactSkyline.toHeaderMode())
        assertEquals(IhsanHeaderMode.Compact, IhsanTopBarStyle.Minimal.toHeaderMode())
    }

    @Test
    fun `header action enforces minimum 48dp touch target and 12dp rounded container`() {
        val action = read("designsystem/src/main/java/com/example/designsystem/component/IhsanHeaderAction.kt")
        assertTrue(action.contains("size: Dp = 48.dp"))
        assertTrue(action.contains("RoundedCornerShape(12.dp)"))
    }

    @Test
    fun `profile header eliminates duplicate title and trims excessive bottom padding`() {
        val profileComponents = read("feature/src/main/java/com/example/feature/profile/presentation/components/ProfileUiComponents.kt")
        val profileScreen = read("feature/src/main/java/com/example/feature/profile/ProfileScreen.kt")
        assertFalse("Duplicate hero title should be removed from ProfileHeroHeader", profileComponents.contains("مساحتك في إحسان"))
        assertFalse("Duplicate hero subtitle should be removed from ProfileHeroHeader", profileComponents.contains("ملفك وتفضيلاتك على هذا الجهاز"))
        assertFalse("Excessive 70.dp bottom padding should be replaced", profileComponents.contains("bottom = 70.dp"))
        assertTrue("ProfileHeroHeader keeps a short bottom inset for the avatar", profileComponents.contains("bottom = 28.dp"))
        assertTrue("Single canonical ProfileTitleHeader remains in ProfileScreen", profileScreen.contains("ProfileTitleHeader()"))
    }

    @Test
    fun `splash logo has no opaque square background in Android 12 theme or Compose splash`() {
        val v31Theme = read("app/src/main/res/values-v31/themes.xml")
        assertTrue(v31Theme.contains("@drawable/splash_ihsan_logo_transparent"))
        assertFalse("No dark square icon background color allowed", v31Theme.contains("windowSplashScreenIconBackgroundColor"))
        assertFalse("No #202427 icon background allowed", v31Theme.contains("#202427"))

        val lightAsset = File(repoRoot, "designsystem/src/main/res/drawable/splash_ihsan_logo_transparent.png")
        val nightAsset = File(repoRoot, "designsystem/src/main/res/drawable-night/splash_ihsan_logo_transparent.png")
        assertTrue(lightAsset.exists() && lightAsset.length() > 0)
        assertTrue(nightAsset.exists() && nightAsset.length() > 0)
    }

    @Test
    fun `quran reader initialLandingDone guard is tied to activeRequestKey`() {
        val readerScreen = read("feature/src/main/java/com/example/feature/quran/presentation/QuranReaderScreen.kt")
        assertTrue(readerScreen.contains("state.activeRequestKey"))
        assertTrue(readerScreen.contains("landedRequestKey == requestIdentity"))
    }
}
