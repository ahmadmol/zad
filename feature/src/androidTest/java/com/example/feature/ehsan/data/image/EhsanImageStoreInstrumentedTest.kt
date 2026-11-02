package com.example.feature.ehsan.data.image

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
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
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        val isolated = File(base.cacheDir, "image-store-test-${java.util.UUID.randomUUID()}").apply { mkdirs() }
        context = object : ContextWrapper(base) {
            override fun getFilesDir() = File(isolated, "files").apply { mkdirs() }
            override fun getCacheDir() = File(isolated, "cache").apply { mkdirs() }
        }
        store = EhsanImageStore(context)
        stagingDir.mkdirs()
    }

    @After
    fun tearDown() {
        // Only the unique synthetic fixture root is removed; never the app's actual image store.
        context.filesDir.parentFile!!.deleteRecursively()
    }

    private fun writeStaged(bytes: ByteArray, ext: String = "jpg"): Uri {
        val f = File(stagingDir, "stage-${System.nanoTime()}.$ext")
        val image = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        val output = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 100, output)
        image.recycle()
        f.writeBytes(output.toByteArray())
        return Uri.fromFile(f)
    }

    @Test
    fun persistedImageIsReadableAcrossAFreshStoreInstance() = runBlocking {
        val source = writeStaged(ByteArray(256) { (it % 251).toByte() })
        val reference = store.persist(source)
        assertNotNull("persist must produce a stable reference", reference)
        assertTrue("reference must use the app-owned prefix", EhsanImageStore.isOwned(reference))

        val resolved = EhsanImageStore(context).resolve(reference)
        assertNotNull("resolve must return a Uri for an app-owned reference", resolved)
        assertTrue(File((resolved as Uri).path!!).length() > 0)
    }

    @Test
    fun resolveReturnsNullForAnOwnedReferenceWhoseFileIsMissing() {
        // No persistence; reference shaped to a non-existent file in the store's
        // directory. resolve() must surface the missing state, not crash.
        val reference = EhsanImageStore.REFERENCE_PREFIX + "does-not-exist.jpg"
        assertNull(store.resolve(reference))
    }

    @Test
    fun resolveReturnsTheLegacyUriForNonOwnedReferences() {
        val legacy = "content://media/external/images/media/42"
        val resolved = store.resolve(legacy)
        assertNotNull(resolved)
        assertEquals(legacy, resolved.toString())
    }

    @Test
    fun foreignLegacyFileUriIsUnavailableWithoutDiscardingItsStoredReference() {
        // The row/reference is retained; arbitrary foreign files are not loaded.
        val legacy = "file:///storage/emulated/0/Deleted.jpg"
        val resolved = store.resolve(legacy)
        assertNull(resolved)
    }

    @Test
    fun nullAndBlankReferencesResolveToNull() {
        assertNull(store.resolve(null))
        assertNull(store.resolve(""))
        assertNull(store.resolve("   "))
    }

    @Test
    fun pathTraversalAttemptsAreRejected() {
        val reference = EhsanImageStore.REFERENCE_PREFIX + "../escape.jpg"
        assertNull(store.resolve(reference))
    }

    @Test
    fun pathSeparatorsInAReferenceAreRejected() {
        val reference = EhsanImageStore.REFERENCE_PREFIX + "sub/inner.jpg"
        assertNull(store.resolve(reference))
    }

    @Test
    fun deleteRemovesTheAppOwnedFile() = runBlocking {
        val source = writeStaged(ByteArray(64) { 7 })
        val reference = store.persist(source)!!
        val resolved = EhsanImageStore(context).resolve(reference)!!
        assertTrue(File((resolved as Uri).path!!).exists())

        val deleted = store.delete(reference)
        assertTrue("delete must report success for an owned reference", deleted)
        assertNull("after deletion resolve must return null", store.resolve(reference))
    }

    @Test
    fun deleteIsANoOpForLegacyReferences() = runBlocking {
        val result = store.delete("content://media/external/images/media/42")
        assertFalse("a legacy uri is not an app-owned file", result)
    }

    @Test
    fun cleanupOrphansRemovesOnlyFilesNotReferenced() = runBlocking {
        val live = store.persist(writeStaged(ByteArray(128) { 1 }))!!
        val orphan = store.persist(writeStaged(ByteArray(128) { 2 }))!!

        val removed = store.cleanupOrphans(referencedBy = listOf(live))
        assertEquals(1, removed)
        assertNotNull(store.resolve(live))
        assertNull(store.resolve(orphan))
    }

    @Test
    fun cleanupOrphansKeepsEveryLiveReferenceAndRemovesNothingElse() = runBlocking {
        val a = store.persist(writeStaged(ByteArray(64) { 3 }))!!
        val b = store.persist(writeStaged(ByteArray(64) { 4 }))!!

        val removed = store.cleanupOrphans(referencedBy = listOf(a, b))
        assertEquals(0, removed)
        assertNotNull(store.resolve(a))
        assertNotNull(store.resolve(b))
    }

    @Test
    fun aPersistedImageSurvivesARelaunchOfTheStore() = runBlocking {
        val source = writeStaged(ByteArray(64) { 5 })
        val reference = store.persist(source)!!
        val newStore = EhsanImageStore(context)
        val resolved = newStore.resolve(reference)
        assertNotNull("the reference must be valid after restart", resolved)
        assertTrue(File((resolved as Uri).path!!).length() > 0)
    }

    private fun runBlocking(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking { block() }
    }
}
