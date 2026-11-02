package com.example.feature.dashboard.presentation

import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Formats clock / prayer times with Arabic ص/م markers (never AM/PM).
 */
object ArabicClockFormatter {

    private val timeNoSeconds = DateTimeFormatter.ofPattern("hh:mm", Locale.US)
    private val timeWithSeconds = DateTimeFormatter.ofPattern("hh:mm:ss", Locale.US)

    fun format(date: Date, includeSeconds: Boolean = false): String {
        val localTime = date.toInstant().atZone(ZoneId.systemDefault())
        val base = if (includeSeconds) {
            timeWithSeconds.format(localTime)
        } else {
            timeNoSeconds.format(localTime)
        }
        val suffix = if (localTime.hour < 12) "ص" else "م"
        return "$base $suffix"
    }

    /** Replaces Latin AM/PM markers if present in an already-formatted string. */
    fun sanitize(value: String): String {
        return value
            .replace(Regex("""\s*AM\b""", RegexOption.IGNORE_CASE), " ص")
            .replace(Regex("""\s*PM\b""", RegexOption.IGNORE_CASE), " م")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }
}
