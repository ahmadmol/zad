package com.example.feature.core.search

import java.text.Normalizer
import java.util.Locale

/** Search keys only. Never persist the normalized form over source religious text. */
object ArabicSearch {
    fun key(value: String): String = buildString {
        Normalizer.normalize(value, Normalizer.Form.NFKD).forEach { ch ->
            val type = Character.getType(ch)
            if (type != Character.NON_SPACING_MARK.toInt() &&
                type != Character.COMBINING_SPACING_MARK.toInt() &&
                type != Character.ENCLOSING_MARK.toInt() && ch != '\u0640' && ch != '\u06E5' && ch != '\u06E6') {
                append(when (ch) { 'ٱ' -> 'ا'; 'ى' -> 'ي'; else -> ch })
            }
        }
    }.lowercase(Locale.ROOT).trim().replace(Regex("\\s+"), " ")

    fun matches(text: String, query: String): Boolean {
        if (query.isBlank()) return true // Empty library filter means show all.
        val words = key(query).split(' ').filter(String::isNotBlank)
        val candidate = key(text)
        return words.isNotEmpty() && words.all(candidate::contains)
    }
}
