package com.example.feature.ehsan.domain.model

/**
 * Phase 8B — typed donation lifecycle.
 *
 * Release 1 is a **local community board**: the status describes coordination between
 * two neighbours, never a verified or paid transaction.
 *
 * ```
 * ACTIVE → COORDINATING → FULFILLED
 *        ↘             ↘ CANCELLED
 *        ↘ EXPIRED
 * ```
 *
 * The Room column stays `TEXT` so no destructive schema change is needed. Legacy
 * free-form values written by earlier builds are mapped by [fromStorage], which is
 * total: an unknown string degrades to [ACTIVE] rather than dropping the row.
 */
enum class DonationStatus(
    /** Value persisted in `donations.status`. Never change these — they are on disk. */
    val storageValue: String
) {
    /** Listed and open. */
    ACTIVE("ACTIVE"),

    /** Someone made contact; the two sides are arranging handover. */
    COORDINATING("COORDINATING"),

    /** Handover happened. Terminal. */
    FULFILLED("FULFILLED"),

    /** Timed out without coordination. Terminal. */
    EXPIRED("EXPIRED"),

    /** Withdrawn by its owner. Terminal. */
    CANCELLED("CANCELLED");

    val isTerminal: Boolean
        get() = this == FULFILLED || this == EXPIRED || this == CANCELLED

    /** Transitions permitted by the lifecycle above. */
    fun canTransitionTo(target: DonationStatus): Boolean = when (this) {
        ACTIVE -> target == COORDINATING || target == FULFILLED ||
            target == EXPIRED || target == CANCELLED
        COORDINATING -> target == FULFILLED || target == CANCELLED || target == ACTIVE
        FULFILLED, EXPIRED, CANCELLED -> false
    }

    companion object {
        /**
         * Maps any persisted value — current or legacy — onto the typed lifecycle.
         *
         * Legacy vocabulary actually written to disk by pre-Phase-8 builds was
         * `AVAILABLE` / `PENDING` / `COMPLETED` (see the old `Donation.status`
         * comment). Those are preserved here so no existing row is lost or
         * misclassified on upgrade.
         */
        fun fromStorage(raw: String?): DonationStatus {
            val key = raw?.trim()?.uppercase().orEmpty()
            return when (key) {
                "ACTIVE", "AVAILABLE", "OPEN", "" -> ACTIVE
                "COORDINATING", "PENDING", "IN_PROGRESS", "RESERVED" -> COORDINATING
                "FULFILLED", "COMPLETED", "DONE", "DELIVERED" -> FULFILLED
                "EXPIRED" -> EXPIRED
                "CANCELLED", "CANCELED", "WITHDRAWN" -> CANCELLED
                else -> ACTIVE
            }
        }

        /** Statuses a user may pick as the next step from [current]. */
        fun nextOptions(current: DonationStatus): List<DonationStatus> =
            entries.filter { current.canTransitionTo(it) }
    }
}
