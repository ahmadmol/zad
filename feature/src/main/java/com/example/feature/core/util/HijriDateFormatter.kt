package com.example.feature.core.util

/** Stateless formatter for callers without preferences; visible Compose dates use currentHijriDateText. */
object HijriDateFormatter {
    fun nowFormatted(): String = nowDateOnly()
    fun nowDateOnly(): String {
        val provider = HijriDateProvider()
        return runCatching { HijriDateProvider.format(provider.date(provider.today())) }
            .getOrElse { "التاريخ الهجري غير متاح" }
    }
}
