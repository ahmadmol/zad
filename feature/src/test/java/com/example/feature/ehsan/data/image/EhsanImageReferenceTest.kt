package com.example.feature.ehsan.data.image

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Phase 8A — reference-format contract for app-owned images.
 *
 * The file I/O paths (`persist`, `resolve`, `delete`, `cleanupOrphans`) need a real
 * `ContentResolver` and `filesDir`, so they are exercised on-device
 * (**RUNTIME VERIFICATION REQUIRED**). What *can* be pinned here without Android is the
 * reference format itself — the thing that is written into the database and therefore
 * the thing that must never change shape.
 */
class EhsanImageReferenceTest {

    @Test
    fun `owned references carry the stable prefix`() {
        assertTrue(EhsanImageStore.isOwned(EhsanImageStore.REFERENCE_PREFIX + "abc.jpg"))
    }

    @Test
    fun `legacy content uris are not treated as owned`() {
        assertFalse(
            EhsanImageStore.isOwned("content://media/external/images/media/42")
        )
        assertFalse(EhsanImageStore.isOwned("file:///storage/emulated/0/x.jpg"))
    }

    @Test
    fun `null and blank references are not owned`() {
        assertFalse(EhsanImageStore.isOwned(null))
        assertFalse(EhsanImageStore.isOwned(""))
    }

    @Test
    fun `the reference prefix is a stable on-disk contract`() {
        // Changing this string orphans every stored image on every installed device.
        // If it must ever change, a data migration is required first.
        assertTrue(EhsanImageStore.REFERENCE_PREFIX == "ihsan-image:")
    }

    @Test
    fun `a reference is not an absolute path`() {
        // Deliberately relative: the app's data directory can move between installs
        // and upgrades, so an absolute path would break.
        val reference = EhsanImageStore.REFERENCE_PREFIX + "abc.jpg"
        assertFalse(reference.contains("/data/"))
        assertFalse(reference.startsWith("/"))
    }
}
