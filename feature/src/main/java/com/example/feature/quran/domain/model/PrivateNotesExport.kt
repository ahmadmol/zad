package com.example.feature.quran.domain.model

import kotlinx.serialization.json.*

/** Exports only explicitly selected private notes; never includes a database or Quran corpus. */
object PrivateNotesExport {
    fun encode(notes: List<PrivateAyahNote>, selected: Set<String>): String {
        require(selected.isNotEmpty())
        val chosen = notes.filter { it.id in selected }
        require(chosen.size == selected.size) { "تغيرت الملاحظات المحددة؛ اخترها مجددًا" }
        return buildJsonObject {
            put("format", "ihsan-private-notes")
            put("version", 1)
            putJsonArray("notes") {
                chosen.forEach { note -> addJsonObject {
                    put("id", note.id); put("surahId", note.surahId); put("ayahNumber", note.ayahNumber)
                    put("text", note.text); put("createdAt", note.createdAt); put("updatedAt", note.updatedAt)
                } }
            }
        }.toString()
    }
}
