package com.example.mol.navigation

/** Child navigation only pushes. Back is always popBackStack, preserving the originating entry. */
fun serviceDestinationRoute(key: String): String? = when (key) {
    "quran" -> Screen.Quran.route
    "azkar" -> Screen.Azkar.route
    "qibla" -> Screen.Qibla.route
    "dua" -> Screen.Dua.route
    "calendar" -> Screen.IslamicCalendar.route
    "asma" -> Screen.Asma.route
    "prayer" -> Screen.Prayer.route
    "tasbih" -> Screen.Tasbih.route
    "hadith" -> Screen.Hadith.route
    "prayer_journal" -> Screen.PrayerJournal.route
    "fasting" -> Screen.FastingJournal.route
    "ramadan" -> Screen.RamadanHub.route
    "memorization" -> Screen.Memorization.route
    "reading_journey" -> Screen.ReadingJourney.createRoute()
    "fahm" -> Screen.FahmMain.route
    "sanhya" -> Screen.SanhyaMain.route
    "nabi_ihsan" -> Screen.NabiIhsanMain.route
    "haram" -> Screen.HaramLive.route
    "nabawi" -> Screen.NabawiLive.route
    "daily" -> Screen.DailyActivities.route
    "statistics" -> Screen.Statistics.route
    "donations" -> Screen.Donations.route
    "reminders" -> Screen.Reminders.route
    "settings" -> Screen.Settings.route
    "search" -> Screen.GlobalSearch.route
    "goals" -> Screen.DailyGoals.route
    "support" -> Screen.Support.route
    "visit" -> Screen.HaramainVisit.route
    else -> null
}
