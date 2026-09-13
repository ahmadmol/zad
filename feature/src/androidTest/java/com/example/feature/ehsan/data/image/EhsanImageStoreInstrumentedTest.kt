package com.example.feature.ehsan.data.image

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Phase 8A — instrumented coverage for the app-owned image store.
 *
 * These tests need a real [Context] (for `filesDir` and `contentResolver`), so
 * they live under `androidTest` rather than `test`. The contract pinned here
 * is the one that broke in pre-Phase-8 builds — a `content://` URI that did
 * not survive process death.
 */
@RunWith(AndroidJUnit4::class)
class EhsanImageStoreInstrumentedTest {

    private lateinit var context: Context
    private lateinit var store: EhsanImageStore
    private val stagingDir: File by lazy { File(context.cacheDir, "image-store-staging") }

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        store = EhsanImageStore(context)
        stagingDir.mkdirs()
    }

    @After
    fun tearDown() {
        stagingDir.deleteRecursively()
        File(context.filesDir, "ehsan_images").deleteRecursively()
    }

    private fun writeStaged(bytes: ByteArray, ext: String = "jpg"): Uri {
        val f = File(stagingDir, "stage-${System.nanoTime()}.$ext")
        f.writeBytes(bytes)
        return Uri.fromFile(f)
    }

    @Test
    fun `persisted image is readable across a fresh store instance`() = runBlocking {
        val source = writeStaged(ByteArray(256) { (it % 251).toByte() })
        val reference = store.persist(source)
        assertNotNull("persist must produce a stable reference", reference)
        assertTrue("reference must use the app-owned prefix", EhsanImageStore.isOwned(reference))

        val resolved = EhsanImageStore(context).resolve(reference)
        assertNotNull("resolve must return a Uri for an app-owned reference", resolved)
        assertEquals(256, File(resolved!!.path!!).length())
    }

    @Test
    fun `resolve returns null for an owned reference whose file is missing`() {
        // No persistence; reference shaped to a non-existent file in the store's
        // directory. resolve() must surface the missing state, not crash.
        val reference = EhsanImageStore.REFERENCE_PREFIX + "does-not-exist.jpg"
        assertNull(store.resolve(reference))
    }

    @Test
    fun `resolve returns the legacy uri for non-owned references`() {
        val legacy = "content://media/external/images/media/42"
        val resolved = store.resolve(legacy)
        assertNotNull(resolved)
        assertEquals(legacy, resolved.toString())
    }

    @Test
    fun `a non-existent legacy uri is still handed back as-is`() {
        // Backward compatibility: the previous app stored raw content:// / file://
        // values. Even when those no longer resolve on this device, the legacy
        // string is returned unchanged so a listing still *shows* something.
        val legacy = "file:///storage/emulated/0/Deleted.jpg"
        val resolved = store.resolve(legacy)
        assertNotNull(resolved)
        assertEquals(legacy, resolved.toString())
    }

    @Test
    fun `null and blank references resolve to null`() {
        assertNull(store.resolve(null))
        assertNull(store.resolve(""))
        assertNull(store.resolve("   "))
    }

    @Test
    fun `path traversal attempts are rejected`() {
        val reference = EhsanImageStore.REFERENCE_PREFIX + "../escape.jpg"
        assertNull(store.resolve(reference))
    }

    @Test
    fun `path separators in a reference are rejected`() {
        val reference = EhsanImageStore.REFERENCE_PREFIX + "sub/inner.jpg"
        assertNull(store.resolve(reference))
    }

    @Test
    fun `delete removes the app-owned file`() = runBlocking {
        val source = writeStaged(ByteArray(64) { 7 })
        val reference = store.persist(source)!!
        val resolved = EhsanImageStore(context).resolve(reference)!!
        assertTrue(File(resolved.path!!).exists())

        val deleted = store.delete(reference)
        assertTrue("delete must report success for an owned reference", deleted)
        assertNull("after deletion resolve must return null", store.resolve(reference))
    }

    @Test
    fun `delete is a no-op for legacy references`() = runBlocking {
        val result = store.delete("content://media/external/images/media/42")
        assertFalse("a legacy uri is not an app-owned file", result)
    }

    @Test
    fun `cleanupOrphans removes only files not referenced`() = runBlocking {
        val live = store.persist(writeStaged(ByteArray(128) { 1 }))!!
        val orphan = store.persist(writeStaged(ByteArray(128) { 2 }))!!

        val removed = store.cleanupOrphans(referencedBy = listOf(live))
        assertEquals(1, removed)
        assertNotNull(store.resolve(live))
        assertNull(store.resolve(orphan))
    }

    @Test
    fun `cleanupOrphans keeps every live reference and removes nothing else`() = runBlocking {
        val a = store.persist(writeStaged(ByteArray(64) { 3 }))!!
        val b = store.persist(writeStaged(ByteArray(64) { 4 }))!!

        val removed = store.cleanupOrphans(referencedBy = listOf(a, b))
        assertEquals(0, removed)
        assertNotNull(store.resolve(a))
        assertNotNull(store.resolve(b))
    }

    @Test
    fun `a persisted image survives a relaunch of the store`() = runBlocking {
        val source = writeStaged(ByteArray(64) { 5 })
        val reference = store.persist(source)!!
        val newStore = EhsanImageStore(context)
        val resolved = newStore.resolve(reference)
        assertNotNull("the reference must be valid after restart", resolved)
        assertEquals(64, File(resolved!!.path!!).length())
    }

    private fun runBlocking(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking { block() }
    }
}
