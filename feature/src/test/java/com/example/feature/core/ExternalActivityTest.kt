package com.example.feature.core

import android.content.Context
import android.content.Intent
import com.example.feature.core.util.openExternalActivity
import io.mockk.*
import org.junit.Test
import org.junit.Assert.*

class ExternalActivityTest {
    @Test fun `security rejection is recoverable and never reported as successful opening`() {
        val context=mockk<Context>();val intent=mockk<Intent>();var errors=0
        every {context.startActivity(intent)} throws SecurityException("fixture private data")
        assertFalse(openExternalActivity(context,intent){errors++})
        assertEquals(1,errors)
        verify(exactly=1) {context.startActivity(intent)}
    }
    @Test fun `a successful open preserves the caller intent and does not signal an error`() {
        val context=mockk<Context>();val intent=mockk<Intent>();var errors=0
        every {context.startActivity(intent)} just Runs
        assertTrue(openExternalActivity(context,intent){errors++})
        assertEquals(0,errors)
        verify(exactly=1) {context.startActivity(intent)}
    }
}
