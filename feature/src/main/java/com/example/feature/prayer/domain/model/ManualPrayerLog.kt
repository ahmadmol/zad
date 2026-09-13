package com.example.feature.prayer.domain.model

import java.time.LocalDate

/**
 * Phase 6 — Manual Prayer Tracker domain model.
 *
 * Product rule (Section 1, locked): the app records only what the user states. It
 * never judges, never marks a prayer missed on its own, and never ranks users.
 */
enum class ManualPrayerStatus {
    /** Prayed — the user marked it done. */
    PERFORMED,

    /** Prayed, but after its window. Recorded only if the user says so. */
    LATE,

    /** The user explicitly recorded not having prayed it. Never inferred. */
    NOT_PERFORMED;

    companion object {
        /** Total mapping from a stored value; unknown values degrade to [PERFORMED]. */
        fun fromStorage(raw: String?): ManualPrayerStatus =
            entries.firstOrNull { it.name == raw?.trim()?.uppercase() } ?: PERFORMED
    }
}

/** A single user-recorded prayer for one day. */
data class ManualPrayerLog(
    val date: LocalDate,
    val prayer: PrayerName,
    val status: ManualPrayerStatus,
    val loggedAtEpochMillis: Long
)

/** The five obligatory prayers — Sunrise is a time marker, never a logged prayer. */
object TrackablePrayers {
    val all: List<PrayerName> = PrayerName.entries.filter { it.isNotifiable }

    fun isTrackable(prayer: PrayerName): Boolean = prayer.isNotifiable
}

/** Aggregated view of one day's manual log, used by Statistics. */
data class ManualPrayerDaySummary(
    val date: LocalDate,
    val logs: Map<PrayerName, ManualPrayerStatus>
) {
    val performedCount: Int
        get() = logs.values.count {
            it == ManualPrayerStatus.PERFORMED || it == ManualPrayerStatus.LATE
        }

    /** How many of the five the user has recorded anything about. */
    val loggedCount: Int get() = logs.size

    /** Not "missed" — simply not recorded yet. */
    val unloggedCount: Int get() = TrackablePrayers.all.size - loggedCount
}
