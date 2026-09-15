package com.example.feature.live

/**
 * Official Saudi Broadcasting Authority live sources for the Two Holy Mosques.
 *
 * Primary: HTTPS HLS (ExoPlayer). Fallback: official YouTube channel live embed.
 * YouTube channel IDs are published on saudiatv.sba.sa.
 */
object LiveStreamSources {
    /** قناة القرآن الكريم — live from Masjid al-Haram (Makkah) */
    const val HARAM_HLS =
        "https://cdn-globecast.akamaized.net/live/eds/saudi_quran/hls_roku/index.m3u8"
    const val HARAM_YOUTUBE_CHANNEL_ID = "UCos52azQNBgW63_9uDJoPDA"

    /** قناة السنة النبوية — live from Masjid an-Nabawi (Madinah) */
    const val NABAWI_HLS =
        "https://cdn-globecast.akamaized.net/live/eds/saudi_sunnah/hls_roku/index.m3u8"
    const val NABAWI_YOUTUBE_CHANNEL_ID = "UCROKYPep-UuODNwyipe6JMw"

    fun youtubeEmbedUrl(channelId: String): String =
        "https://www.youtube.com/embed/live_stream?channel=$channelId&autoplay=1&playsinline=1&rel=0"

    fun youtubeChannelLiveUrl(channelId: String): String =
        "https://www.youtube.com/channel/$channelId/live"
}

enum class LiveSourceType(
    val id: String,
    val tabTitle: String,
    val screenTitle: String,
    val sourceName: String,
    val description: String,
    val hlsUrl: String,
    val youtubeChannelId: String,
    val locationName: String
) {
    HARAM(
        id = "haram",
        tabTitle = "المسجد الحرام",
        screenTitle = "البث المباشر - المسجد الحرام",
        sourceName = "المسجد الحرام - مكة المكرمة",
        description = "شاهد البث المباشر من بيت الله الحرام",
        hlsUrl = LiveStreamSources.HARAM_HLS,
        youtubeChannelId = LiveStreamSources.HARAM_YOUTUBE_CHANNEL_ID,
        locationName = "مكة المكرمة"
    ),
    NABAWI(
        id = "nabawi",
        tabTitle = "المسجد النبوي",
        screenTitle = "البث المباشر - المسجد النبوي",
        sourceName = "المسجد النبوي - المدينة المنورة",
        description = "شاهد البث المباشر من المسجد النبوي الشريف",
        hlsUrl = LiveStreamSources.NABAWI_HLS,
        youtubeChannelId = LiveStreamSources.NABAWI_YOUTUBE_CHANNEL_ID,
        locationName = "المدينة المنورة"
    );

    companion object {
        fun fromUrlOrTitle(hlsUrl: String?, title: String?): LiveSourceType {
            return if (hlsUrl?.contains("saudi_sunnah") == true ||
                title?.contains("النبوي") == true ||
                title?.contains("nabawi", ignoreCase = true) == true) {
                NABAWI
            } else {
                HARAM
            }
        }
    }
}
