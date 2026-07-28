package com.example.feature.dashboard.presentation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Formats clock / prayer times with Arabic ص/م markers (never AM/PM).
 */
object ArabicClockFormatter {

    private val timeNoSeconds = SimpleDateFormat("hh:mm", Locale.US)
    private val timeWithSeconds = SimpleDateFormat("hh:mm:ss", Locale.US)

    fun format(date: Date, includeSeconds: Boolean = false): String {
        val base = if (includeSeconds) {
            timeWithSeconds.format(date)
        } else {
            timeNoSeconds.format(date)
        }
        val calendar = Calendar.getInstance().apply { time = date }
        val suffix = if (calendar.get(Calendar.AM_PM) == Calendar.AM) "ص" else "م"
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
