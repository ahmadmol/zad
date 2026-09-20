package com.example.feature.sanhya.data.datasource

import com.example.feature.sanhya.domain.model.QuranStory
import com.example.feature.sanhya.domain.model.StoryEpisode

/** Official catalog for "قصص القرآن - الجزء الأول"; the feature name remains "سنحيا بالقرآن". */
object SanhyaFixtureData {
    const val PROGRAM_URL = "https://amrkhaled.net/program/1168/قصص-القرآن---الجزء-الأول"
    const val PLAYLIST_ID = "PLDF02D375CDBA4851"
    const val OFFICIAL_CHANNEL_ID = "UCjxcF4A7pCyWsuJcZOymF_g"

    private data class EpisodeRecord(
        val number: Int,
        val title: String,
        val videoId: String?,
        val duration: String?,
        val publishedAt: String?,
        val sourceEpisodeId: String,
        val embeddable: Boolean = videoId != null
    )

    private fun episode(record: EpisodeRecord, partLabel: String? = null) = StoryEpisode(
        id = "ep_${record.number.toString().padStart(2, '0')}",
        episodeNumber = record.number,
        title = record.title,
        partLabel = partLabel,
        youtubeVideoId = record.videoId,
        durationLabel = record.duration,
        thumbnailUrl = record.videoId?.let { "https://i.ytimg.com/vi/$it/hqdefault.jpg" },
        publishedAt = record.publishedAt,
        sourceUrl = "https://amrkhaled.net/episode/${record.sourceEpisodeId}",
        playlistId = PLAYLIST_ID,
        playlistPosition = record.number,
        sourceChannelId = OFFICIAL_CHANNEL_ID,
        isEmbeddable = record.embeddable,
        isAvailable = record.videoId != null
    )

    private val records = listOf(
        EpisodeRecord(1, "برنامج  قصص القرآن الجزء الاول | الحلقة الاولى(1) سنحيا بالقرآن | Stories from Qur'an EP 1", "Kx9Wj4vP69A", "47:45", "2009-06-14T05:03:02-07:00", "1029242"),
        EpisodeRecord(2, "1برنامج  قصص القرآن الجزء الأول | الحلقة الثانية (2) قصة ابنى ادم | Stories from Qur'an EP 2", "Ugh3nGLgZXY", "48:26", "2009-06-14T07:45:40-07:00", "1029241"),
        EpisodeRecord(3, "2 برنامج  قصص القرآن الجزء الأول | الحلقة الثالثة (3) قصة ابنى ادم | Stories from Qur'an EP 3", "ALiJwsKl-Tg", "47:50", "2009-06-14T10:13:58-07:00", "1029240"),
        EpisodeRecord(4, "برنامج  قصص القرآن الجزء الأول | الحلقة الرابعة (4) قصة اصحاب الجنة | Stories from Qur'an EP 4", "Er0EPYkCKu0", "49:31", "2009-06-14T13:39:16-07:00", "1029239"),
        EpisodeRecord(5, "1قصص القرآن الجزء الأول | الحلقة الخامسة (5) قصة الغلام والراهب والساحر | Stories fromQur'an EP5", "35trZgpKGoo", "48:38", "2009-06-14T14:08:37-07:00", "1029238"),
        EpisodeRecord(6, "قصص القرآن الجزء الأول | الحلقة السادسة (6)  قصة الغلام والراهب والساحر2 | Stories fromQur'an EP6", "WZnSTPuqhXg", "47:56", "2009-06-14T16:17:53-07:00", "1029237"),
        EpisodeRecord(7, "قصص القرآن الجزء الأول | عمرو خالد | الحلقة السابعة (7) قصة السيدة مريم  | Stories fromQur'an EP7", "adr8HqnAwRU", "47:47", "2009-06-15T02:54:59-07:00", "1029236"),
        EpisodeRecord(8, "قصص القرآن الجزء الأول| عمرو خالد | الحلقة الثامنة (8) قصة طالوت وجالوت | Stories fromQur'an EP8", "rbL99lJa31o", "48:09", "2009-06-15T05:04:19-07:00", "1029235"),
        EpisodeRecord(9, "قصص القرآن الجزء الأول| عمرو خالد | الحلقة التاسعة (9) قصة أصحاب الكهف 1 | Stories fromQur'an EP9", "QjTLJLkwvSk", "48:23", "2009-06-15T07:46:18-07:00", "1029234"),
        EpisodeRecord(10, "قصص القرآن الجزء الأول| عمرو خالد | الحلقة العاشرة (10) قصة أصحاب الكهف 2 | Stories fromQur'an EP10", "UVc9YjMloZg", "52:42", "2009-06-15T10:44:45-07:00", "1029233"),
        EpisodeRecord(11, "قصص القرآن الجزء الأول | الحلقة الحادية عشر (11) قصة الدرع المسروقة   | Stories fromQur'an EP11", "Gnr9znaOgO8", "47:45", "2009-06-15T13:09:37-07:00", "1029232"),
        EpisodeRecord(12, "قصص القرآن الجزء الأول| عمرو خالد | الحلقة الثانية عشر (12) قصة قارون | Stories fromQur'an EP12", "QALvk--o7kc", "47:55", "2009-06-15T15:16:51-07:00", "1029231"),
        EpisodeRecord(13, "قصص القرآن الجزء الأول | الحلقة الثالثة عشر (13)  قصة نبى الله عزير | Stories fromQur'an EP 13", "y3PiB5eMMK0", "47:39", "2009-06-15T17:33:32-07:00", "1029230"),
        EpisodeRecord(14, "قصص القرآن الجزء الأول | الحلقة الرابعة عشر (14) قصة بلعام بن باعوراء | Stories fromQur'an EP 14", "vPUEamXZCbI", "47:24", "2009-06-15T21:14:05-07:00", "1029229"),
        EpisodeRecord(15, "قصص القرآن الجزء الأول| عمرو خالد| الحلقة الخامسة عشر (15) قصة كعب بن مالك |Stories fromQur'an EP 15", "1N7MTKOPYc8", "49:10", "2009-06-15T21:07:53-07:00", "1029228"),
        EpisodeRecord(16, "قصص القرآن ج1 | الحلقة السادسة عشر (16) قصة مؤمن ياسين والهدهد والنملة | Stories fromQur'an EP 16", "VkDN_ZMceBQ", "48:12", "2009-06-16T07:46:28-07:00", "1029227"),
        EpisodeRecord(17, "قصص القرآن الجزء الاول  | الحلقة السابعة عشر (17) قصة موسى والخضر ج1 | Stories fromQur'an EP 17", "7zdwIlEnZ3M", "47:56", "2009-06-16T10:07:20-07:00", "1029226"),
        EpisodeRecord(18, "قصص القرآن الجزء الاول | الحلقة الثامنة عشر (18) قصة موسى والخضر ج2 | Stories fromQur'an EP 18", "ZvAZaau77gk", "48:14", "2009-06-16T12:15:42-07:00", "1029225"),
        EpisodeRecord(19, "قصص القرآن ج1 | الحلقة التاسعة عشر (19) بداية العشر الآواخر من رمضان | Stories fromQur'an EP 19", "DDVgES67Qf4", "45:15", "2009-06-16T14:24:38-07:00", "1029224"),
        EpisodeRecord(20, "قصص القرآن الجزء الاول | الحلقة العشرون (20) قصة حادثة الإفك | Stories fromQur'an EP 20", "6iwUrrrhJ2g", "47:19", "2009-06-16T16:02:21-07:00", "1029223"),
        EpisodeRecord(21, "قصص القرآن الجزء الاول | الحلقة الثامنة عشر (18) قصة ذوالقرنين | Stories fromQur'an EP 18", "1gCdxksI9TI", "47:24", "2009-06-16T17:59:06-07:00", "1029222"),
        EpisodeRecord(22, "قصص القرآن الجزء الاول | الحلقة الثانية والعشرون (22) عبادة الدعاء | Stories fromQur'an EP 22", "_AFo3bGvpJw", "47:50", "2009-06-16T19:59:24-07:00", "1029221"),
        EpisodeRecord(23, "قصص القرآن الجزء الاول | الحلقة الثالثة والعشرون (23) قصة لقمان الحكيم | Stories fromQur'an EP 23", "jEHVJSMhfio", "48:47", "2009-06-16T21:43:45-07:00", "1029220"),
        EpisodeRecord(24, "قصص القرآن الجزء الاول | الحلقة الرابعة والعشرون (24) قصة يوم التناد | Stories fromQur'an EP 24", "YwAkepuLtTo", "47:46", "2009-06-16T23:31:12-07:00", "1029219"),
        EpisodeRecord(25, "قصص القرآن الجزء الاول | الحلقة الخامسة والعشرون (25) قصة آل عمران | Stories fromQur'an EP 25", "6f0gHOw20Jc", "47:34", "2009-06-17T02:47:49-07:00", "1029218"),
        EpisodeRecord(26, "قصص القرآن الجزء الاول | الحلقة السادسة والعشرون (26) اسم الله العفو | Stories fromQur'an EP 26", "jCrRcbcJkuQ", "42:07", "2009-06-17T07:28:17-07:00", "1029217"),
        // The official index/playlist points slot 27 to an unrelated "على خطى الحبيب" upload.
        EpisodeRecord(27, "1برنامج على خطى الحبيب | الحلقة السابعة والعشرون (27) كيف نثبت بعد رمضان  |Ala Khota Al Habeeb EP 27", null, null, null, "1029216", embeddable = false),
        EpisodeRecord(28, "قصص القرآن الجزء الاول | الحلقة الثامنة والعشرون (٢٨) كيف نثبت بعد رمضان | Stories fromQur'an EP 28", "S97S7QbGIZ0", "41:16", "2009-06-17T13:02:06-07:00", "1029215"),
        EpisodeRecord(29, "3قصص القرآن الجزء الاول |الحلقة التاسعة والعشرون (٢٩)  كيف نثبت بعد رمضان | Stories fromQur'an EP 28", "_s7K9TWNKX8", "43:01", "2009-06-17T14:36:25-07:00", "1029214")
    )

    private fun topic(id: String, title: String, category: String, numbers: IntRange) = QuranStory(
        id = id,
        title = title,
        subtitle = "",
        summary = "",
        coverUrl = "",
        surahs = emptyList(),
        category = category,
        quranReferences = emptyList(),
        lessons = emptyList(),
        episodes = numbers.map { number ->
            episode(records.single { it.number == number }, if (numbers.count() > 1) "الجزء ${number - numbers.first + 1}" else null)
        },
        sourceTitle = "قصص القرآن - الجزء الأول",
        sourceAuthor = "د. عمرو خالد",
        keywords = listOf(title)
    )

    val defaultStories = listOf(
        topic("topic_sanhya", "سنحيا بالقرآن", "موضوع", 1..1),
        topic("story_adam_sons", "ابنا آدم", "قصة", 2..3),
        topic("story_garden_owners", "أصحاب الجنة", "قصة", 4..4),
        topic("story_boy_monk_magician", "الغلام والراهب والساحر", "قصة", 5..6),
        topic("story_maryam", "السيدة مريم", "قصة", 7..7),
        topic("story_talut_jalut", "طالوت وجالوت", "قصة", 8..8),
        topic("story_cave_companions", "أصحاب الكهف", "قصة", 9..10),
        topic("story_stolen_armor", "الدرع المسروقة", "قصة", 11..11),
        topic("story_qarun", "قارون", "قصة", 12..12),
        topic("story_uzair", "نبي الله عزير", "قصة", 13..13),
        topic("story_balaam", "بلعام بن باعوراء", "قصة", 14..14),
        topic("story_kaab", "كعب بن مالك", "قصة", 15..15),
        topic("topic_yasin_hoopoe_ant", "مؤمن ياسين والهدهد والنملة", "موضوع قصصي", 16..16),
        topic("story_moses_khidr", "موسى والخضر", "قصة", 17..18),
        topic("topic_last_ten", "بداية العشر الأواخر من رمضان", "موضوع", 19..19),
        topic("story_ifk", "حادثة الإفك", "قصة", 20..20),
        topic("story_dhul_qarnayn", "ذو القرنين", "قصة", 21..21),
        topic("topic_dua", "عبادة الدعاء", "موضوع", 22..22),
        topic("story_luqman", "لقمان الحكيم", "قصة", 23..23),
        topic("topic_day_of_calling", "يوم التناد", "موضوع", 24..24),
        topic("story_al_imran", "آل عمران", "قصة", 25..25),
        topic("topic_al_afu", "اسم الله العفو", "موضوع", 26..26),
        topic("topic_steadfast_after_ramadan", "كيف نثبت بعد رمضان", "موضوع", 27..29)
    )

    val categories = listOf("الكل", "قصة", "موضوع قصصي", "موضوع")
}
