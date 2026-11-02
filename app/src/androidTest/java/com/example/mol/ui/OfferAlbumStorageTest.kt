package com.example.mol.ui

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.ehsan.data.image.EhsanImageStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.After
import org.junit.Test
import java.io.File

/** Uses an isolated directory and a real Android ContentResolver; never deletes app data. */
class OfferAlbumStorageTest {
    private lateinit var sandbox: File
    private lateinit var store: EhsanImageStore
    @Before fun setup() {
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        sandbox = File(target.cacheDir, "album-test-${System.nanoTime()}").apply { mkdirs() }
        store = EhsanImageStore(object : ContextWrapper(target) { override fun getFilesDir() = sandbox })
    }
    @After fun cleanup() { sandbox.deleteRecursively() }
    private fun source(name: String): Uri {
        val file = File(sandbox, name)
        val bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        return Uri.fromFile(file)
    }
    @Test fun albumSurvivesReopeningAndKeepsCoverAndOrder() = runBlocking {
        val album = store.persistAlbum(listOf(source("a.png"), source("b.png"), source("c.png")))!!
        val refs = store.imageReferences(album)
        assertEquals(3, refs.size)
        val reopened = EhsanImageStore(object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
            override fun getFilesDir() = sandbox
        })
        assertEquals(refs, reopened.imageReferences(album))
        assertEquals(reopened.resolve(refs.first()).toString(), reopened.resolve(album).toString())
        refs.forEach { assertNotNull(reopened.resolve(it)) }
        assertTrue(store.delete(album))
        refs.forEach { assertNull(store.resolve(it)) }
        assertTrue(store.imageReferences(album).isEmpty())
    }
    @Test fun singleAndLegacyReferencesRemainCompatible() = runBlocking {
        val image = store.persistAlbum(listOf(source("single.png")))!!
        assertTrue(image.startsWith(EhsanImageStore.REFERENCE_PREFIX))
        assertEquals(listOf(image), store.imageReferences(image))
        assertNotNull(store.resolve(image))
        assertEquals(listOf("drawable/ihsan_ehsan_school_backpack"), store.imageReferences("drawable/ihsan_ehsan_school_backpack"))
        assertNotNull(store.resolve("drawable/ihsan_ehsan_school_backpack"))
    }
    @Test fun partialCopyFailureRollsBackNewFiles() = runBlocking {
        val image = source("valid.png")
        val missing = Uri.fromFile(File(sandbox, "missing.png"))
        assertNull(store.persistAlbum(listOf(image, missing)))
        assertTrue(File(sandbox, "ehsan_images").listFiles().orEmpty().isEmpty())
    }
    @Test fun orphanCleanupPreservesWholeReferencedAlbum() = runBlocking {
        val album = store.persistAlbum(listOf(source("a.png"), source("b.png")))!!
        val orphan = store.persist(source("orphan.png"))!!
        assertEquals(1, store.cleanupOrphans(listOf(album)))
        assertNull(store.resolve(orphan))
        store.imageReferences(album).forEach { assertNotNull(store.resolve(it)) }
        assertEquals(3, store.cleanupOrphans(emptyList()))
    }
    @Test fun missingCorruptAndUnsafeManifestsDoNotResolveOutsideStore() {
        assertTrue(store.imageReferences("ihsan-album:missing.json").isEmpty())
        assertNull(store.resolve("ihsan-album:../outside.json"))
        val directory = File(sandbox, "ehsan_images").apply { mkdirs() }
        File(directory, "bad.json").writeText("[\"ihsan-image:../outside.jpg\"]")
        assertNull(store.resolve("ihsan-album:bad.json"))
        File(directory, "bad.json").writeText("invalid")
        assertTrue(store.imageReferences("ihsan-album:bad.json").isEmpty())
    }
}
