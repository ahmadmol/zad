package com.example.feature.quran.presentation

/**
 * Pinch zoom commits one font size when the fingers lift. While the gesture moves,
 * only a graphics-layer scale changes, so the Quran text is not remeasured per event.
 * One-finger paging stays available at the fitted size.
 */
internal object QuranZoomPolicy {
    const val MIN_FONT = 20f
    const val MAX_FONT = 48f

    fun committedFontSize(currentFontSize: Float, gestureScale: Float): Float =
        (currentFontSize * gestureScale).coerceIn(MIN_FONT, MAX_FONT)

    fun blocksHorizontalPaging(liveZoom: Float, pressedPointers: Int): Boolean =
        pressedPointers >= 2 || liveZoom > 1.02f
}
