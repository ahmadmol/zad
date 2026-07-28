package com.example.designsystem.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class HomeVisualCorrectionBoundaryTest {

    private val header = File(
        "src/main/java/com/example/designsystem/component/DashboardHeader.kt"
    ).readText()

    private val daily = File(
        "src/main/java/com/example/designsystem/component/DailyActivityCard.kt"
    ).readText()

    private val bottomNav = File(
        "src/main/java/com/example/designsystem/component/IhsanBottomNavigation.kt"
    ).readText()

    @Test
    fun `prayer cards do not use AM PM labels`() {
        assertFalse(header.contains("text = \"AM\""))
        assertFalse(header.contains("text = \"PM\""))
        assertTrue(header.contains("width(78.dp)") || header.contains("widthIn(min ="))
        assertTrue(header.contains("LazyRow"))
        // Suffix helper may mention AM/PM only for conversion to ص/م.
        assertTrue(header.contains("\"ص\"") || header.contains("ص"))
    }

    @Test
    fun `clock keeps hours and minutes together`() {
        assertTrue(header.contains("substringBeforeLast(' '"))
        assertFalse(header.contains("substringBeforeLast(':', trimmed)"))
    }

    @Test
    fun `daily activity orders progress and open-list for RTL`() {
        assertTrue(daily.contains("التقدم العام"))
        assertTrue(daily.contains("فتح القائمة"))
        val progressIdx = daily.indexOf("التقدم العام")
        val openIdx = daily.indexOf("فتح القائمة")
        // In the bottom Row, التقدم العام appears before فتح القائمة so RTL places it on the right.
        assertTrue(progressIdx < openIdx)
        assertTrue(daily.contains("StrokeCap.Butt"))
    }

    @Test
    fun `bottom navigation never coerces unknown index to Home`() {
        assertFalse(bottomNav.contains("selectedIndex.coerceIn(0"))
        assertTrue(bottomNav.contains("require(selectedIndex in destinations.indices)"))
        assertTrue(bottomNav.contains("IhsanBottomNavDestination"))
    }
}
