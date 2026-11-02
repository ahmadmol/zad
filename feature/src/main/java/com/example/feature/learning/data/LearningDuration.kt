package com.example.feature.learning.data

/** Parses catalog timestamps without treating unavailable duration as a short lesson. */
internal object LearningDuration {
    val filters = listOf("الكل", "أقل من 20 دقيقة", "20 - 40 دقيقة", "أكثر من 40 دقيقة")

    fun seconds(label: String?): Long? {
        val parts = label?.trim()?.split(':') ?: return null
        if (parts.size !in 2..3) return null
        val values = parts.map { part ->
            if (part.isEmpty() || part.any { it.digitToIntOrNull() == null }) return null
            part.map { it.digitToInt() }.joinToString("").toLongOrNull() ?: return null
        }
        if (values.last() !in 0..59 || (values.size == 3 && values[1] !in 0..59)) return null
        val hours = if (values.size == 3) values[0] else 0
        val minutes = values[values.lastIndex - 1]
        // Catalog values are bounded before multiplication, including malformed imports.
        if (hours > Int.MAX_VALUE || minutes > Int.MAX_VALUE) return null
        return hours * 3600 + minutes * 60 + values.last()
    }

    fun matches(label: String?, filter: String): Boolean {
        if (filter !in filters.drop(1)) return true
        val seconds = seconds(label) ?: return false
        return when (filter) {
            filters[1] -> seconds < 20 * 60
            filters[2] -> seconds in 1200L..2400L
            else -> seconds > 40 * 60
        }
    }
}
