package com.example.feature.quran.domain.model

/**
 * Phase 4 — Quran Repeat Range.
 *
 * Pure domain description of "what should play next" while a repeat is active. Kept
 * free of Media3 / Android types so it can be unit-tested exhaustively and so the
 * existing local-first audio pipeline ([com.example.feature.quran.util.QuranAudioSourceResolver])
 * stays the single source of truth for *where* the audio comes from.
 */
enum class QuranRepeatMode {
    /** No repeat — sequential playback to the end of the surah. */
    OFF,

    /** Repeat the ayah that is currently playing. */
    SINGLE_AYAH,

    /** Repeat a closed [QuranRepeatConfig.startAyah]..[QuranRepeatConfig.endAyah] range. */
    RANGE
}

/**
 * Repeat configuration for the reader currently open.
 *
 * [repeatCount] is the number of times the unit (ayah or range) is played in total.
 * [UNLIMITED] means "keep repeating until the user stops".
 */
data class QuranRepeatConfig(
    val mode: QuranRepeatMode = QuranRepeatMode.OFF,
    val startAyah: Int = 1,
    val endAyah: Int = 1,
    val repeatCount: Int = UNLIMITED,
    /** Completed passes of the unit so far. Reset whenever the config changes. */
    val completedCycles: Int = 0
) {
    val isActive: Boolean get() = mode != QuranRepeatMode.OFF

    companion object {
        const val UNLIMITED = 0

        val OFF = QuranRepeatConfig()

        /**
         * Builds a normalized RANGE config. The bounds are ordered and clamped into
         * `1..ayahCount`, so an inverted or out-of-range user selection can never
         * produce a plan that seeks outside the surah.
         */
        fun range(
            start: Int,
            end: Int,
            ayahCount: Int,
            repeatCount: Int = UNLIMITED
        ): QuranRepeatConfig {
            val upperBound = ayahCount.coerceAtLeast(1)
            val lo = minOf(start, end).coerceIn(1, upperBound)
            val hi = maxOf(start, end).coerceIn(1, upperBound)
            return QuranRepeatConfig(
                mode = QuranRepeatMode.RANGE,
                startAyah = lo,
                endAyah = hi,
                repeatCount = repeatCount.coerceAtLeast(UNLIMITED)
            )
        }

        /** Builds a normalized SINGLE_AYAH config for [ayah]. */
        fun singleAyah(
            ayah: Int,
            ayahCount: Int,
            repeatCount: Int = UNLIMITED
        ): QuranRepeatConfig {
            val bounded = ayah.coerceIn(1, ayahCount.coerceAtLeast(1))
            return QuranRepeatConfig(
                mode = QuranRepeatMode.SINGLE_AYAH,
                startAyah = bounded,
                endAyah = bounded,
                repeatCount = repeatCount.coerceAtLeast(UNLIMITED)
            )
        }
    }
}

/** What the player should do once the current ayah finishes. */
sealed interface QuranRepeatDecision {
    /** Play [ayah] next. [config] carries the updated cycle counter. */
    data class Play(val ayah: Int, val config: QuranRepeatConfig) : QuranRepeatDecision

    /** Stop cleanly — the repeat finished its requested count, or playback ran out. */
    data class Stop(val config: QuranRepeatConfig) : QuranRepeatDecision
}

/**
 * Decides the next ayah given the repeat configuration. Deterministic and side-effect
 * free — the ViewModel only translates the decision into a `playAyah` / stop call.
 */
object QuranRepeatPlanner {

    /**
     * @param config current repeat configuration.
     * @param currentAyah the ayah that just finished playing (1-based).
     * @param ayahCount number of ayahs in the open surah.
     */
    fun decideNext(
        config: QuranRepeatConfig,
        currentAyah: Int,
        ayahCount: Int
    ): QuranRepeatDecision {
        if (ayahCount <= 0) return QuranRepeatDecision.Stop(config)

        return when (config.mode) {
            QuranRepeatMode.OFF ->
                if (currentAyah < ayahCount) {
                    QuranRepeatDecision.Play(currentAyah + 1, config)
                } else {
                    QuranRepeatDecision.Stop(config)
                }

            QuranRepeatMode.SINGLE_AYAH -> {
                val cycles = config.completedCycles + 1
                if (isFinished(config, cycles)) {
                    QuranRepeatDecision.Stop(QuranRepeatConfig.OFF)
                } else {
                    QuranRepeatDecision.Play(
                        config.startAyah.coerceIn(1, ayahCount),
                        config.copy(completedCycles = cycles)
                    )
                }
            }

            QuranRepeatMode.RANGE -> {
                val lo = config.startAyah.coerceIn(1, ayahCount)
                val hi = config.endAyah.coerceIn(lo, ayahCount)

                // Playback drifted outside the range (for example the user tapped an
                // ayah past the range): re-enter the range at its start rather than
                // silently abandoning the repeat.
                if (currentAyah < lo || currentAyah > hi) {
                    return QuranRepeatDecision.Play(lo, config)
                }

                if (currentAyah < hi) {
                    QuranRepeatDecision.Play(currentAyah + 1, config)
                } else {
                    val cycles = config.completedCycles + 1
                    if (isFinished(config, cycles)) {
                        QuranRepeatDecision.Stop(QuranRepeatConfig.OFF)
                    } else {
                        QuranRepeatDecision.Play(lo, config.copy(completedCycles = cycles))
                    }
                }
            }
        }
    }

    private fun isFinished(config: QuranRepeatConfig, completedCycles: Int): Boolean =
        config.repeatCount != QuranRepeatConfig.UNLIMITED &&
            completedCycles >= config.repeatCount
}
