package com.example.feature.nabiihsan.util

/**
 * Cleans episode titles for display by removing redundant episode prefixes,
 * hashtags, and formatting glitches while preserving original title semantics.
 */
fun cleanEpisodeTitle(title: String, @Suppress("UNUSED_PARAMETER") episodeNumber: Int? = null): String {
    if (title.isBlank()) return title
    var cleaned = title
    cleaned = cleaned.replace(Regex("#\\S+"), "")
    cleaned = cleaned.replace(Regex("^(حلقة|الحلقة)\\s*[0-9\\u0660-\\u0669]+\\s*[/\\-:|]?\\s*"), "")
    cleaned = cleaned.replace(Regex("\\.{2,}"), ".")
    cleaned = cleaned.replace(Regex("\\s+\\."), ".")
    cleaned = cleaned.replace(Regex("\\.([^\\s.])"), ". $1")
    cleaned = cleaned.trim().removePrefix("/").removePrefix("-").removePrefix(":").removePrefix("—").trim()
    return if (cleaned.isBlank()) title else cleaned
}
