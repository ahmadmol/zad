package com.example.feature.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectivityMonitorTest {

    @Test
    fun `online and offline state are exposed consistently`() {
        val monitor = FakeConnectivityMonitor()

        assertEquals(ConnectivityStatus.Offline, monitor.status.value)
        assertFalse(monitor.isOnline.value)

        monitor.setOnline(true)
        assertEquals(ConnectivityStatus.Online, monitor.status.value)
        assertTrue(monitor.isOnline.value)

        monitor.setOnline(false)
        assertEquals(ConnectivityStatus.Offline, monitor.status.value)
        assertFalse(monitor.isOnline.value)
    }

    private class FakeConnectivityMonitor : ConnectivityMonitor {
        private val state = MutableStateFlow(ConnectivityStatus.Offline)
        private val online = MutableStateFlow(false)
        override val status: StateFlow<ConnectivityStatus> = state.asStateFlow()
        override val isOnline: StateFlow<Boolean> = online.asStateFlow()

        fun setOnline(online: Boolean) {
            state.value = if (online) ConnectivityStatus.Online else ConnectivityStatus.Offline
            this.online.value = online
        }
    }
}
