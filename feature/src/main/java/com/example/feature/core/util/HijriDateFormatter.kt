package com.example.feature.core.util

import androidx.annotation.RequiresApi
import android.os.Build
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.util.Calendar

/**
 * Formats the current Hijri date into Arabic text.
 *
 * Uses `java.time.chrono.HijrahDate` when available (API 26+, or via core library
 * desugaring on lower SDKs). Falls back to a simple Gregorian-only string on
 * very old devices that don't support the Hijri chronology.
 */
object HijriDateFormatter {

    private val arabicMonths = listOf(
        "محرّم", "صفر", "ربيع الأول", "ربيع الآخر",
        "جمادى الأولى", "جمادى الآخرة", "رجب", "شعبان",
        "رمضان", "شوّال", "ذو القعدة", "ذو الحجة"
    )

    private val arabicWeekdays = mapOf(
        Calendar.SATURDAY to "السبت",
        Calendar.SUNDAY to "الأحد",
        Calendar.MONDAY to "الإثنين",
        Calendar.TUESDAY to "الثلاثاء",
        Calendar.WEDNESDAY to "الأربعاء",
        Calendar.THURSDAY to "الخميس",
        Calendar.FRIDAY to "الجمعة"
    )

    /** Returns e.g. "الجمعة، ٢٢ ربيع الأول ١٤٤٦". */
    fun nowFormatted(): String {
        val weekday = arabicWeekdays[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)] ?: ""
        val datePart = nowDateOnly()
        return if (weekday.isNotEmpty()) "$weekday، $datePart" else datePart
    }

    /** Returns e.g. "٢٢ ربيع الأول ١٤٤٦" (no weekday). */
    fun nowDateOnly(): String {
        return runCatching { hijriFromJavaTime() }.getOrElse { fallbackHijri() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun hijriFromJavaTime(): String {
        val date = HijrahDate.now()
        val day = date.get(ChronoField.DAY_OF_MONTH)
        val month = date.get(ChronoField.MONTH_OF_YEAR)
        val year = date.get(ChronoField.YEAR)
        return "${day.toArabicDigits()} ${arabicMonths[month - 1]} ${year.toArabicDigits()}"
    }

    private fun fallbackHijri(): String {
        // Last-resort fallback: just say "اليوم" if Hijri chronology isn't reachable.
        return "اليوم"
    }

    private fun Int.toArabicDigits(): String {
        val arabicNumerals = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { c ->
            if (c.isDigit()) arabicNumerals[c.digitToInt()] else c
        }.joinToString("")
    }
}
