package com.example.feature.ehsan.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Phase 8B — typed lifecycle + legacy-row preservation. */
class DonationStatusTest {

    // --- Legacy preservation ----------------------------------------------

    @Test
    fun `legacy vocabulary maps onto the typed lifecycle`() {
        assertEquals(DonationStatus.ACTIVE, DonationStatus.fromStorage("AVAILABLE"))
        assertEquals(DonationStatus.COORDINATING, DonationStatus.fromStorage("PENDING"))
        assertEquals(DonationStatus.FULFILLED, DonationStatus.fromStorage("COMPLETED"))
    }

    @Test
    fun `mapping is case and whitespace insensitive`() {
        assertEquals(DonationStatus.FULFILLED, DonationStatus.fromStorage("  completed "))
        assertEquals(DonationStatus.COORDINATING, DonationStatus.fromStorage("Pending"))
    }

    @Test
    fun `unknown null and blank values degrade to active rather than dropping the row`() {
        assertEquals(DonationStatus.ACTIVE, DonationStatus.fromStorage(null))
        assertEquals(DonationStatus.ACTIVE, DonationStatus.fromStorage(""))
        assertEquals(DonationStatus.ACTIVE, DonationStatus.fromStorage("something-unexpected"))
    }

    @Test
    fun `round trip through storage is stable for every status`() {
        DonationStatus.entries.forEach { status ->
            assertEquals(status, DonationStatus.fromStorage(status.storageValue))
        }
    }

    @Test
    fun `storage values are unique and non blank`() {
        val values = DonationStatus.entries.map { it.storageValue }
        assertEquals(values.size, values.toSet().size)
        assertTrue(values.none { it.isBlank() })
    }

    // --- Lifecycle rules ---------------------------------------------------

    @Test
    fun `active can move to coordinating`() {
        assertTrue(DonationStatus.ACTIVE.canTransitionTo(DonationStatus.COORDINATING))
    }

    @Test
    fun `coordinating can be fulfilled or cancelled or reopened`() {
        val s = DonationStatus.COORDINATING
        assertTrue(s.canTransitionTo(DonationStatus.FULFILLED))
        assertTrue(s.canTransitionTo(DonationStatus.CANCELLED))
        assertTrue(s.canTransitionTo(DonationStatus.ACTIVE))
    }

    @Test
    fun `coordinating cannot expire directly`() {
        assertFalse(DonationStatus.COORDINATING.canTransitionTo(DonationStatus.EXPIRED))
    }

    @Test
    fun `terminal states reject every transition`() {
        listOf(DonationStatus.FULFILLED, DonationStatus.EXPIRED, DonationStatus.CANCELLED)
            .forEach { terminal ->
                assertTrue(terminal.isTerminal)
                DonationStatus.entries.forEach { target ->
                    assertFalse(
                        terminal.name + " -> " + target.name + " must be rejected",
                        terminal.canTransitionTo(target)
                    )
                }
            }
    }

    @Test
    fun `no status can transition to itself`() {
        DonationStatus.entries.forEach {
            assertFalse(it.canTransitionTo(it))
        }
    }

    @Test
    fun `next options match the transition rules`() {
        DonationStatus.entries.forEach { current ->
            val options = DonationStatus.nextOptions(current)
            assertTrue(options.all { current.canTransitionTo(it) })
            assertEquals(
                DonationStatus.entries.count { current.canTransitionTo(it) },
                options.size
            )
        }
    }

    @Test
    fun `active is not terminal`() {
        assertFalse(DonationStatus.ACTIVE.isTerminal)
        assertFalse(DonationStatus.COORDINATING.isTerminal)
    }
}
