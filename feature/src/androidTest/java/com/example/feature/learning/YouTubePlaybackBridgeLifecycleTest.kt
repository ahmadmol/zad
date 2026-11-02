package com.example.feature.learning

import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.learning.presentation.components.YouTubePlaybackBridge
import org.junit.Assert.assertEquals
import org.junit.Test

class YouTubePlaybackBridgeLifecycleTest {
    @Test fun closedPlayerCannotDeliverPendingOrFuturePlaybackCallbacks() {
        val instrumentation=InstrumentationRegistry.getInstrumentation()
        var plays=0
        lateinit var bridge:YouTubePlaybackBridge
        instrumentation.runOnMainSync {
            bridge=YouTubePlaybackBridge({},{plays++},{},{})
            bridge.playing()
            bridge.close()
            bridge.playing()
        }
        instrumentation.waitForIdleSync()
        assertEquals(0,plays)
    }
}
