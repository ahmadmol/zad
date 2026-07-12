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
