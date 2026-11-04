package com.example.feature.prayer.domain.location

/** Decides whether a cached device fix may be used for prayer calculations. */
object DeviceLocationFreshness {
    const val MAX_AGE_MILLIS: Long = 30L * 60L * 1000L

    fun isFresh(fixEpochMillis: Long, nowEpochMillis: Long, maxAgeMillis: Long = MAX_AGE_MILLIS): Boolean {
        if (fixEpochMillis <= 0L) return false
        val age = nowEpochMillis - fixEpochMillis
        return age in 0L..maxAgeMillis
    }

    /**
     * A stale cache is never promoted to the current location when the fresh request fails.
     * Repeated calls with the same stale cache keep rejecting it.
     */
    fun choose(hasCachedFix: Boolean, cachedFresh: Boolean, freshFixAvailable: Boolean): FixChoice = when {
        cachedFresh && hasCachedFix -> FixChoice.UseCached
        freshFixAvailable -> FixChoice.UseFresh
        hasCachedFix -> FixChoice.RejectStale
        else -> FixChoice.Missing
    }

    enum class FixChoice { UseCached, UseFresh, RejectStale, Missing }
}
