package com.example.feature.ehsan.data.image

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Phase 8A — stable, app-owned image persistence for Ehsan listings.
 *
 * Earlier builds stored the raw `content://` URI returned by
 * `ActivityResultContracts.GetContent()`. That grant is scoped to the picking
 * activity and is revoked on process death / reboot, so a listing's photo silently
 * became unopenable. This store copies the picked bytes once into app-private
 * storage and persists a **stable relative reference** instead.
 *
 * Persisted form: `ihsan-image:<fileName>` — deliberately not an absolute path, so
 * the reference survives the app's data directory moving between installs/upgrades.
 * Legacy raw `content://` and `file://` values are still readable via [resolve], so
 * no existing listing is broken by the change.
 */
class EhsanImageStore(
    private val context: Context
) {

    /** Directory holding listing images. Created lazily. */
    private val imagesDir: File
        get() = File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /**
     * Copies [source] into app-owned storage.
     *
     * @return the stable reference to persist, or `null` if the source could not be
     *   read (revoked grant, deleted file). Callers should treat `null` as "no image"
     *   rather than falling back to the unstable URI.
     */
    suspend fun persist(source: Uri): String? = withContext(Dispatchers.IO) {
        val fileName = UUID.randomUUID().toString() + extensionFor(source)
        val target = File(imagesDir, fileName)
        try {
            context.contentResolver.openInputStream(source)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
        } catch (e: IOException) {
            target.delete()
            return@withContext null
        } catch (e: SecurityException) {
            // The transient read grant was already revoked.
            target.delete()
            return@withContext null
        }

        if (target.length() == 0L) {
            target.delete()
            return@withContext null
        }
        REFERENCE_PREFIX + fileName
    }

    /**
     * Resolves a persisted reference to something loadable.
     *
     * Accepts app-owned references and, for backward compatibility, the legacy raw
     * URI strings written before Phase 8. Returns `null` when an app-owned file is
     * missing, so the UI can show its empty state instead of a broken image.
     */
    fun resolve(reference: String?): Uri? {
        if (reference.isNullOrBlank()) return null
        if (!reference.startsWith(REFERENCE_PREFIX)) {
            // Legacy value (content:// or file://) — hand it back unchanged.
            return runCatching { Uri.parse(reference) }.getOrNull()
        }
        val file = fileFor(reference) ?: return null
        return if (file.exists()) Uri.fromFile(file) else null
    }

    /** Deletes the image owned by [reference]. No-op for legacy/foreign references. */
    suspend fun delete(reference: String?): Boolean = withContext(Dispatchers.IO) {
        val file = fileFor(reference) ?: return@withContext false
        file.exists() && file.delete()
    }

    /**
     * Removes app-owned images that no listing points at any more.
     *
     * @param referencedBy every image reference currently stored in the database.
     * @return the number of orphan files deleted.
     */
    suspend fun cleanupOrphans(referencedBy: Collection<String?>): Int =
        withContext(Dispatchers.IO) {
            val live = referencedBy
                .filterNotNull()
                .filter { it.startsWith(REFERENCE_PREFIX) }
                .map { it.removePrefix(REFERENCE_PREFIX) }
                .toSet()

            imagesDir.listFiles()
                .orEmpty()
                .filter { it.isFile && it.name !in live }
                .count { it.delete() }
        }

    /** Resolves a reference to its backing file, guarding against path traversal. */
    private fun fileFor(reference: String?): File? {
        if (reference == null || !reference.startsWith(REFERENCE_PREFIX)) return null
        val name = reference.removePrefix(REFERENCE_PREFIX)
        if (name.isBlank() || name.contains('/') || name.contains('\\') || name.contains("..")) {
            return null
        }
        return File(imagesDir, name)
    }

    private fun extensionFor(source: Uri): String {
        val mime = runCatching { context.contentResolver.getType(source) }.getOrNull()
        return when (mime) {
            "image/png" -> ".png"
            "image/webp" -> ".webp"
            "image/gif" -> ".gif"
            else -> ".jpg"
        }
    }

    companion object {
        const val REFERENCE_PREFIX = "ihsan-image:"
        private const val DIR_NAME = "ehsan_images"

        /** True when [reference] is owned by this store (as opposed to a legacy URI). */
        fun isOwned(reference: String?): Boolean =
            reference != null && reference.startsWith(REFERENCE_PREFIX)
    }
}
