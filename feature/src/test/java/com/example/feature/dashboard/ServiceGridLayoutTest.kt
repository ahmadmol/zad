package com.example.feature.dashboard

import com.example.feature.dashboard.presentation.components.serviceGridColumns
import org.junit.Assert.assertEquals
import org.junit.Test

class ServiceGridLayoutTest {
    @Test fun `regular phone shows three columns but narrow windows keep readable tiles`() {
        assertEquals(3, serviceGridColumns(328f, 1f))
        assertEquals(2, serviceGridColumns(240f, 1f))
        assertEquals(1, serviceGridColumns(160f, 1f))
    }

    @Test fun `large Arabic text gets additional space per tile`() {
        assertEquals(2, serviceGridColumns(328f, 1.3f))
        assertEquals(1, serviceGridColumns(328f, 2f))
        assertEquals(3, serviceGridColumns(720f, 2f))
    }

    @Test fun `extreme window sizes always retain a usable column count`() {
        assertEquals(1, serviceGridColumns(0f, 3f))
        assertEquals(3, serviceGridColumns(2000f, 1f))
        assertEquals(3, serviceGridColumns(328f, .85f))
    }
}
