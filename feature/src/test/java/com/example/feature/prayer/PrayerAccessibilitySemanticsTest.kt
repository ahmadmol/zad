package com.example.feature.prayer

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * P1-04 — accessibility semantics guards for the prayer screen.
 *
 * The Active Prayer card and the System Status section both expose a
 * `contentDescription` on an outer container while also rendering the same
 * information as visible `Text` children. Without `mergeDescendants = true`
 * TalkBack reads both layers, producing a doubled announcement such as
 *
 *   "العصر، الوقت المتبقي 02:13، الرياض، العصر، ASR، الوقت المتبقي، 02:13، …".
 *
 * This test pins the structural fix so a future refactor that drops
 * `mergeDescendants` (or adds a redundant outer label) fails the build.
 */
class PrayerAccessibilitySemanticsTest {

    private val repoRoot = File(".").canonicalFile.let { dir ->
        generateSequence(dir) { it.parentFile }
            .firstOrNull { File(it, "settings.gradle.kts").exists() }
            ?: dir
    }

    private val screen by lazy {
        File(
            repoRoot,
            "feature/src/main/java/com/example/feature/prayer/PrayerScreen.kt"
        ).readText()
    }

    @Test
    fun `active prayer card merges semantics to prevent double-read`() {
        // The card sets a contentDescription that already names the prayer,
        // countdown and location. Without mergeDescendants TalkBack would
        // walk the inner Text("العصر"), Text("ASR"), Text("الوقت المتبقي"),
        // Text("02:13") and the location row separately.
        val activeMarker = "fun ActivePrayerCard("
        val activeIdx = screen.indexOf(activeMarker)
        assertTrue("ActivePrayerCard missing in PrayerScreen.kt", activeIdx >= 0)
        val activeBody = screen.substring(activeIdx, screen.indexOf("fun ", activeIdx + activeMarker.length).let {
            if (it < 0) screen.length else it
        })
        assertTrue(
            "ActivePrayerCard must use semantics(mergeDescendants = true) so children are not double-read",
            activeBody.contains("semantics(mergeDescendants = true)")
        )
        assertTrue(
            "ActivePrayerCard must still publish a unified contentDescription",
            activeBody.contains("contentDescription = prayer.nameAr")
        )
    }

    @Test
    fun `system status section header merges semantics to prevent double-read`() {
        // The diagnostics row has contentDescription = headerText while also
        // rendering headerText as a Text() child. Without mergeDescendants,
        // TalkBack reads the title twice ("إعدادات متقدمة، اضغط لعرض...").
        val diagnosticsMarker = "fun PrayerSystemStatusSection("
        val idx = screen.indexOf(diagnosticsMarker)
        assertTrue("PrayerSystemStatusSection missing in PrayerScreen.kt", idx >= 0)
        val end = screen.indexOf("fun ", idx + diagnosticsMarker.length).let {
            if (it < 0) screen.length else it
        }
        val body = screen.substring(idx, end)
        assertTrue(
            "PrayerSystemStatusSection must use semantics(mergeDescendants = true) so children are not double-read",
            body.contains("semantics(mergeDescendants = true)")
        )
        // The label must now mention the hint and the expand/collapse action
        // so the merged node carries the full information TalkBack needs.
        assertTrue(
            "system status contentDescription must include the header hint",
            body.contains("headerHint")
        )
    }

    @Test
    fun `the prayer screen does not add redundant outer labels on top of children`() {
        // Defensive: a future contributor may add a new outer container with
        // a contentDescription that duplicates a child. This test catches
        // the simple case of a contentDescription on the row before the
        // header Text() is added. Today the only such labelled rows are the
        // ones handled by the two tests above.
        val labelledRows = Regex(
            """\.semantics\s*\{\s*contentDescription\s*="""
        ).findAll(screen).count()
        // Two intentional labelled rows: the prayer-mode icon button and the
        // active prayer card (which itself is now merged). The system status
        // row is now `semantics(mergeDescendants = true)` so it is not
        // counted by this regex.
        assertTrue(
            "Found an unexpected number of plain semantics blocks; review the prayer screen for double-read risk",
            labelledRows <= 1
        )
    }

    @Test
    fun `prayer system status never relies on text duplicate of headerText outside semantics`() {
        // The header Text() is fine — what matters is that the outer
        // semantics now includes the hint so the merge is informationally
        // complete, not just suppressing children.
        val diagnosticsMarker = "fun PrayerSystemStatusSection("
        val idx = screen.indexOf(diagnosticsMarker)
        assertTrue("PrayerSystemStatusSection missing in PrayerScreen.kt", idx >= 0)
        val end = screen.indexOf("fun ", idx + diagnosticsMarker.length).let {
            if (it < 0) screen.length else it
        }
        val body = screen.substring(idx, end)
        // The body must still contain the visible "headerText" Text() so the
        // user can see the section title, but the row-level contentDescription
        // must not just echo headerText alone (the original double-read bug).
        // The body is the file text, so we look for the multiline occurrence
        // the source actually uses.
        assertTrue(
            "PrayerSystemStatusSection must still render headerText visibly",
            Regex("""Text\(\s*headerText,""").containsMatchIn(body)
        )
        assertFalse(
            "system status contentDescription must not be just headerText alone",
            Regex("""\.semantics[^{]*\{\s*contentDescription\s*=\s*headerText\s*\}""")
                .containsMatchIn(body)
        )
    }
}
