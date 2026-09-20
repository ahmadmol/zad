package com.example.mol.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Splash : Screen(
        route = "splash_screen",
        title = "بداية",
        icon = Icons.Default.Star
    )
    object Home : Screen(
        route = "home_screen",
        title = "الرئيسية",
        icon = Icons.Default.Home
    )
    object Asma : Screen(
        route = "asma_screen",
        title = "الأسماء",
        icon = Icons.Default.AutoAwesome
    )
    object Prayer : Screen(
        route = "prayer_screen",
        title = "الصلاة",
        icon = Icons.Default.AccessTime
    )
    object Quran : Screen(
        route = "quran_list",
        title = "القرآن",
        icon = Icons.AutoMirrored.Filled.MenuBook
    )
    object QuranReader : Screen(
        route = "quran_reader/{surahId}?ayahNumber={ayahNumber}",
        title = "القراءة",
        icon = Icons.AutoMirrored.Filled.MenuBook
    ) {
        fun createRoute(surahId: Int, ayahNumber: Int? = null) = 
            if (ayahNumber != null) "quran_reader/$surahId?ayahNumber=$ayahNumber"
            else "quran_reader/$surahId"
    }
    object Azkar : Screen(
        route = "azkar_screen",
        title = "الأذكار",
        icon = Icons.Default.AutoStories
    )
    object Hadith : Screen(
        route = "hadith_screen",
        title = "الأحاديث",
        icon = Icons.Default.AutoStories
    )
    object Dua : Screen(
        route = "dua_screen",
        title = "الأدعية",
        icon = Icons.Default.AutoAwesome
    )
    object DuaDetail : Screen(
        route = "dua_detail/{duaId}",
        title = "تفاصيل الدعاء",
        icon = Icons.Default.AutoAwesome
    ) {
        fun createRoute(duaId: Long) = "dua_detail/$duaId"
    }
    object Settings : Screen(
        route = "settings_screen",
        title = "الإعدادات",
        icon = Icons.Default.Settings
    )
    object Statistics : Screen(
        route = "statistics_screen",
        title = "الإحصائيات",
        icon = Icons.Default.BarChart
    )
    object Reminders : Screen(
        route = "reminders_screen",
        title = "التنبيهات",
        icon = Icons.Default.Notifications
    )
    object Donations : Screen(
        route = "donations_screen",
        title = "إحسان",
        icon = Icons.Default.Favorite
    )
    // Inbox deferred: no messaging implementation. Do not reintroduce without a real contract.
    object Profile : Screen(
        route = "profile_screen",
        title = "حسابي",
        icon = Icons.Default.Person
    )
    object DonationHistory : Screen(
        route = "donation_history",
        title = "سجل التبرعات",
        icon = Icons.Default.History
    )
    object AddDonation : Screen(
        route = "add_donation_screen/{type}",
        title = "إضافة إحسان",
        icon = Icons.Default.Add
    ) {
        fun createRoute(type: String) = "add_donation_screen/$type"
    }
    object RequestHelp : Screen(
        route = "request_help_screen",
        title = "طلب مساعدة",
        icon = Icons.Default.Handshake
    )
    /**
     * Canonical donation/request details destination.
     * Legacy `donation_detail_screen/{id}` was unreachable and removed in Part 3.
     */
    object IhsanDetails : Screen(
        route = "ihsan_details/{id}",
        title = "تفاصيل الإحسان",
        icon = Icons.Default.Info
    ) {
        fun createRoute(id: Long) = "ihsan_details/$id"
    }

    /**
     * Compatibility redirect for any saved deep links to the legacy donation detail route.
     * Renders nothing; [AppNavHost] immediately navigates to [IhsanDetails].
     * Remove once no compatibility need remains.
     */
    @Deprecated("Use Screen.IhsanDetails; redirect-only legacy route")
    object LegacyDonationDetail : Screen(
        route = "donation_detail_screen/{id}",
        title = "تفاصيل الإحسان",
        icon = Icons.Default.Info
    ) {
        fun createRoute(id: Long) = "donation_detail_screen/$id"
    }
    object EditProfile : Screen(
        route = "edit_profile",
        title = "تعديل الملف الشخصي",
        icon = Icons.Default.Edit
    )
    object Qibla : Screen(
        route = "qibla_screen",
        title = "بوصلة القبلة",
        icon = Icons.Default.Explore
    )
    object Onboarding : Screen(
        route = "onboarding_screen",
        title = "الترحيب",
        icon = Icons.Default.Explore
    )
    object LocationPermission : Screen(
        route = "location_permission_screen",
        title = "الإذن",
        icon = Icons.Default.MyLocation
    )
    object GlobalSearch : Screen(
        route = "global_search",
        title = "بحث شامل",
        icon = Icons.Default.Search
    )
    object DailyActivities : Screen(
        route = "daily_activities",
        title = "النشاطات اليومية",
        icon = Icons.AutoMirrored.Filled.List
    )
    object Tasbih : Screen(
        route = "tasbih_screen",
        title = "تسبيح",
        icon = Icons.Default.BrightnessLow
    )
    /**
     * Controlled Daily read-only experience (Stage 1).
     * Registered only when [com.example.feature.ihsanplus.integration.flags.IhsanPlusFeatureFlags.dailyEnabled].
     */
    object IhsanPlusDaily : Screen(
        route = "ihsan_plus_daily",
        title = "تجربة اليوم",
        icon = Icons.Default.Today
    )
    object HaramLive : Screen(
        route = "haram_live",
        title = "بث مباشر الحرم المكي",
        icon = Icons.Default.LiveTv
    )
    object NabawiLive : Screen(
        route = "nabawi_live",
        title = "بث مباشر المسجد النبوي",
        icon = Icons.Default.LiveTv
    )
    object SanhyaMain : Screen(
        route = "sanhya_main",
        title = "سنحيا بالقرآن",
        icon = Icons.AutoMirrored.Filled.MenuBook
    )
    object SanhyaStoryDetail : Screen(
        route = "sanhya_story_detail/{storyId}",
        title = "تفاصيل القصة",
        icon = Icons.Default.Book
    ) {
        fun createRoute(storyId: String) = "sanhya_story_detail/$storyId"
    }
    object SanhyaEpisodes : Screen(
        route = "sanhya_episodes/{storyId}",
        title = "حلقات القصة",
        icon = Icons.Default.PlayCircle
    ) {
        fun createRoute(storyId: String) = "sanhya_episodes/$storyId"
    }
    object SanhyaQuranVerses : Screen(
        route = "sanhya_quran_verses/{storyId}",
        title = "آيات القصة",
        icon = Icons.AutoMirrored.Filled.MenuBook
    ) {
        fun createRoute(storyId: String) = "sanhya_quran_verses/$storyId"
    }
    object SanhyaFavorites : Screen(
        route = "sanhya_favorites",
        title = "قصصي المفضلة",
        icon = Icons.Default.Favorite
    )
    object SanhyaWatchLater : Screen(
        route = "sanhya_watch_later",
        title = "المشاهدة لاحقاً",
        icon = Icons.Default.WatchLater
    )
    object SanhyaSearch : Screen(
        route = "sanhya_search?query={query}",
        title = "بحث سنحيا بالقرآن",
        icon = Icons.Default.Search
    ) {
        fun createRoute(query: String = "") = "sanhya_search?query=$query"
    }
    object SanhyaSettings : Screen(
        route = "sanhya_settings",
        title = "إعدادات سنحيا بالقرآن",
        icon = Icons.Default.Settings
    )
    object NabiIhsanMain : Screen(
        route = "nabi_ihsan_main",
        title = "نبي الإحسان",
        icon = Icons.Default.AutoAwesome
    )
    object NabiIhsanEpisodeDetails : Screen(
        route = "nabi_ihsan_episode_details/{episodeId}",
        title = "تفاصيل الحلقة",
        icon = Icons.Default.PlayCircle
    ) {
        fun createRoute(episodeId: String) = "nabi_ihsan_episode_details/$episodeId"
    }
    object NabiIhsanEpisodes : Screen(
        route = "nabi_ihsan_episodes",
        title = "حلقات السلسلة",
        icon = Icons.Default.FormatListNumbered
    )
    object NabiIhsanRecipes : Screen(
        route = "nabi_ihsan_recipes",
        title = "الوصفات النبوية",
        icon = Icons.Default.VolunteerActivism
    )
    object NabiIhsanRecipeDetails : Screen(
        route = "nabi_ihsan_recipe_details/{recipeId}",
        title = "تفاصيل الوصفة",
        icon = Icons.Default.Info
    ) {
        fun createRoute(recipeId: String) = "nabi_ihsan_recipe_details/$recipeId"
    }
    object NabiIhsanFavorites : Screen(
        route = "nabi_ihsan_favorites",
        title = "المفضلة",
        icon = Icons.Default.Favorite
    )
    object NabiIhsanWatchLater : Screen(
        route = "nabi_ihsan_watch_later",
        title = "المشاهدة لاحقاً",
        icon = Icons.Default.WatchLater
    )
    object NabiIhsanSearch : Screen(
        route = "nabi_ihsan_search?query={query}",
        title = "بحث نبي الإحسان",
        icon = Icons.Default.Search
    ) {
        fun createRoute(query: String = "") = "nabi_ihsan_search?query=$query"
    }
    object NabiIhsanSettings : Screen(
        route = "nabi_ihsan_settings",
        title = "إعدادات نبي الإحسان",
        icon = Icons.Default.Settings
    )

    // الفهم عن الله - الجزء الأول
    object FahmMain : Screen(
        route = "fahm_main",
        title = "الفهم عن الله",
        icon = Icons.Default.SelfImprovement
    )
    object FahmEpisodes : Screen(
        route = "fahm_episodes",
        title = "جميع الدروس",
        icon = Icons.AutoMirrored.Filled.List
    )
    object FahmEpisodeDetail : Screen(
        route = "fahm_episode_detail/{episodeId}",
        title = "تفاصيل الدرس",
        icon = Icons.Default.PlayCircle
    ) {
        fun createRoute(episodeId: String) = "fahm_episode_detail/$episodeId"
    }
    object FahmStations : Screen(
        route = "fahm_stations",
        title = "منازل الرحلة",
        icon = Icons.Default.Star
    )
    object FahmJourney : Screen(
        route = "fahm_journey",
        title = "رحلتي",
        icon = Icons.Default.Explore
    )
    object FahmSearch : Screen(
        route = "fahm_search?query={query}",
        title = "بحث الفهم عن الله",
        icon = Icons.Default.Search
    ) {
        fun createRoute(query: String = "") = "fahm_search?query=$query"
    }
    object FahmSaved : Screen(
        route = "fahm_saved?tab={tab}",
        title = "المحفوظات",
        icon = Icons.Default.Bookmark
    ) {
        fun createRoute(tab: Int = 0) = "fahm_saved?tab=$tab"
    }
    object FahmCompletion : Screen(
        route = "fahm_completion/{episodeId}",
        title = "إتمام الدرس",
        icon = Icons.Default.CheckCircle
    ) {
        fun createRoute(episodeId: String) = "fahm_completion/$episodeId"
    }
    object FahmAbout : Screen(
        route = "fahm_about",
        title = "عن البرنامج",
        icon = Icons.Default.Info
    )
    object FahmSettings : Screen(
        route = "fahm_settings",
        title = "إعدادات القسم",
        icon = Icons.Default.Settings
    )

    companion object {
        val items: List<Screen>
            get() = listOf(Home, Donations, Profile)
    }
}
