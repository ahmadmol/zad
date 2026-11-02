package com.example.mol.widget
import com.example.mol.navigation.Screen
/** Validated local resume only; never accepts a caller-supplied route or raw URI. */
internal fun widgetReadingRoute(position: Pair<Int, Int>?, verseCount: Int?): String =
    if (position != null && position.first in 1..114 && verseCount != null && position.second in 1..verseCount)
        Screen.QuranReader.createRoute(position.first, position.second) else Screen.Quran.route
