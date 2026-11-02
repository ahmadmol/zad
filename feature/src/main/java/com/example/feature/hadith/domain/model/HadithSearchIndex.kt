package com.example.feature.hadith.domain.model

import com.example.feature.core.search.ArabicSearch
import kotlinx.coroutines.*

/** Bounded prepared keys for the existing edition; never replaces religious source text. */
class HadithSearchIndex {
    private data class Entry(val fields: List<String>, val keys: List<String>)
    private var entries = emptyMap<Long, Entry>()
    suspend fun filter(rows: List<Hadith>, query: String): List<Hadith> = withContext(Dispatchers.Default) {
        if (query.isBlank()) return@withContext rows
        val words = ArabicSearch.key(query).split(' ').filter(String::isNotBlank)
        if (words.isEmpty()) return@withContext emptyList()
        val next = LinkedHashMap<Long, Entry>()
        val result = ArrayList<Hadith>()
        rows.forEachIndexed { index, row ->
            if (index % 64 == 0) ensureActive()
            val fields = listOf(row.text, row.narrator, row.source, row.category)
            val cached = entries[row.id]?.takeIf { it.fields == fields } ?: Entry(fields, fields.map(ArabicSearch::key))
            if (next.size < 4096) next[row.id] = cached
            if (cached.keys.any { key -> words.all(key::contains) }) result += row
        }
        ensureActive()
        entries = next
        result
    }
}
