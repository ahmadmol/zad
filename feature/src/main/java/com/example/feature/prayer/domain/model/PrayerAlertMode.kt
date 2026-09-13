package com.example.feature.prayer.domain.model

/**
 * Phase 6 — per-prayer alert mode.
 *
 * Chosen by the user for each obligatory prayer. This controls **only** the prayer's
 * own EXACT alert. Pre-prayer, iqamah and end-of-window reminders keep their existing
 * notice semantics and are never adhan-capable, whatever mode is selected.
 */
enum class PrayerAlertMode {
    /** No alert at all — the EXACT alarm is not scheduled for this prayer. */
    MUTED,

    /** A quiet notification on the notice channel. Never the adhan recording. */
    NOTICE,

    /** The adhan recording, subject to the user's global sound preference. */
    ADHAN;

    companion object {
        /** Total mapping; an unknown stored value degrades to the default [ADHAN]. */
        fun fromStorage(raw: String?): PrayerAlertMode =
            entries.firstOrNull { it.name == raw?.trim()?.uppercase() } ?: ADHAN
    }
}

/**
 * The user's per-prayer mode selection.
 *
 * Sunrise is a time marker, not a prayer: it can never be ADHAN. Requesting ADHAN for a
 * non-notifiable prayer is silently downgraded to [PrayerAlertMode.NOTICE] here, which
 * is a second line of defence behind [PrayerAlertAudioPolicy].
 */
data class PrayerNotificationModes(
    private val modes: Map<PrayerName, PrayerAlertMode> = emptyMap()
) {
    fun modeFor(prayer: PrayerName): PrayerAlertMode {
        val requested = modes[prayer] ?: default(prayer)
        return if (!prayer.isNotifiable && requested == PrayerAlertMode.ADHAN) {
            PrayerAlertMode.NOTICE
        } else {
            requested
        }
    }

    /** True when this prayer should not be scheduled at all. */
    fun isMuted(prayer: PrayerName): Boolean = modeFor(prayer) == PrayerAlertMode.MUTED

    fun with(prayer: PrayerName, mode: PrayerAlertMode): PrayerNotificationModes =
        PrayerNotificationModes(modes + (prayer to mode))

    fun asMap(): Map<PrayerName, PrayerAlertMode> =
        PrayerName.entries.associateWith { modeFor(it) }

    companion object {
        /** Historical behaviour: adhan for the five prayers, a quiet notice for sunrise. */
        fun default(prayer: PrayerName): PrayerAlertMode =
            if (prayer.isNotifiable) PrayerAlertMode.ADHAN else PrayerAlertMode.NOTICE

        val DEFAULT = PrayerNotificationModes()

        /** Rebuilds from persisted `PRAYER_NAME:MODE` pairs, ignoring unknown names. */
        fun fromStorage(entries: Set<String>): PrayerNotificationModes {
            val parsed = entries.mapNotNull { entry ->
                val parts = entry.split(':', limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val prayer = runCatching { PrayerName.valueOf(parts[0]) }.getOrNull()
                    ?: return@mapNotNull null
                prayer to PrayerAlertMode.fromStorage(parts[1])
            }.toMap()
            return PrayerNotificationModes(parsed)
        }
    }

    /** Serializes to `PRAYER_NAME:MODE` pairs for DataStore. */
    fun toStorage(): Set<String> =
        modes.map { (prayer, mode) -> prayer.name + ":" + mode.name }.toSet()
}
