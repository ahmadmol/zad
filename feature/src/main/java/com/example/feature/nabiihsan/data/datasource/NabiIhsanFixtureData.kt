package com.example.feature.nabiihsan.data.datasource

import com.example.feature.nabiihsan.domain.model.NabiIhsanEpisode
import com.example.feature.nabiihsan.domain.model.PropheticRecipe

/** Official numbered episodes for the 2025 "نبي الإحسان" program. */
object NabiIhsanFixtureData {
    const val PROGRAM_URL = "https://amrkhaled.net/program/1179/نبي-الإحسان"
    const val PLAYLIST_ID = "PLhbs8A5De9zQFjJmTAsZnLPwKcmdfySlG"
    const val OFFICIAL_CHANNEL_ID = "UCjxcF4A7pCyWsuJcZOymF_g"

    private data class EpisodeRecord(
        val number: Int,
        val title: String,
        val videoId: String,
        val duration: String,
        val publishedAt: String,
        val sourceEpisodeId: String,
        val playlistPosition: Int
    )

    private val records = listOf(
        EpisodeRecord(1, "حلقة١ - طريقة نبوية لسلامتك الداخلية وصحتك النفسية..وازاي تعيش يومك مثل رسول الله؟ #نبي_الإحسان", "SGSfIJOiFmc", "42:51", "2025-02-28", "1029501", 58),
        EpisodeRecord(2, "حلقة 2-لو تايه ومتخبط في الدنيا..خطة نبوية محكمة تدير بها حياتك..لا تتنازل عنها أبدًا ؟ #نبي_الإحسان", "Xxnj9TV9XtM", "35:25", "2025-03-02", "1029502", 57),
        EpisodeRecord(3, "حلقة 3 - مفاتيح الرزق الواسع على طريقة رسول الله..المثلث الذهبي للأرزاق .. #نبي_الإحسان", "bktou86QT88", "39:36", "2025-03-03", "1029503", 56),
        EpisodeRecord(4, "حلقة 4 - ثلاثية الحماية من خداع النفس والصراع الداخلي  .. #نبي_الإحسان", "8b9Yu6-JWBI", "39:34", "2025-03-04", "1029504", 55),
        EpisodeRecord(5, "حلقة 5 - 3 أمراض مانعة للهداية .... و3 صفات تجعلك رفيق النبي في الجنة  .. #نبي_الإحسان", "in9ksqjdL5Q", "40:37", "2025-03-05", "1029505", 54),
        EpisodeRecord(6, "حلقة 6 - اوعى الدنيا تكسرك .. ازاي تجاهد نفسك وسط صعوبات ومصائب الحياة؟  #نبي_الإحسان", "A6K2bvU6Zgw", "41:09", "2025-03-06", "1029506", 51),
        EpisodeRecord(7, "حلقة 7 / التعامل مع تقلبات الحياة ..استراتيجية نبوية هتغير حياتك.. لن تتركها .. #نبي_الإحسان", "XRLbiuKkHPY", "39:44", "2025-03-07", "1029507", 48),
        EpisodeRecord(8, "حلقة 8 / احمي علاقاتك من الإنهيار ..طريقة النبي لكسب قلوب الناس .. #نبي_الإحسان", "1AHbcZfhV1c", "40:40", "2025-03-08", "1029508", 46),
        EpisodeRecord(9, "حلقة 9 / ليست غضب من الله.. أحسن طريقة للتعامل مع ابتلاءات الحياة .. #نبي_الإحسان", "T20BWH6nOHg", "40:04", "2025-03-09", "1029509", 45),
        EpisodeRecord(10, "حلقة 10 / حياتنا مليانة أوجاع وتحديات.. طريقة الوصول لأحسن عيشة .. #نبي_الإحسان", "A7PIp2iaw4A", "38:29", "2025-03-10", "1029510", 44),
        EpisodeRecord(11, "حلقة 11 / ازاي تتعامل مع الشخصيات السامة والمؤذية؟– درس خصوصي من رسول الله.. #نبي_الإحسان", "N5FjmQfxzho", "38:49", "2025-03-11", "1029511", 43),
        EpisodeRecord(12, "حلقة 12/ مرض العصر.. التفكير الزائد والتوتر والقلق ( over thinking ) .. #نبي_الإحسان", "05Zil2gZob4", "39:17", "2025-03-12", "1029512", 42),
        EpisodeRecord(13, "حلقة 13/ حلاوة وأسرار اللقاء الأول مع رسول الله ﷺ.. #نبي_الإحسان.", "AL7VG6D_i4Y", "39:51", "2025-03-13", "1029513", 41),
        EpisodeRecord(14, "حلقة 14/ روشتة من سنة رسول الله لمواجهة الإحباط واليأس ..#نبي_الإحسان", "tZzsSQuh5u4", "38:50", "2025-03-14", "1029514", 40),
        EpisodeRecord(15, "حلقة 15/ اوعى تبيع اللي اشتروك.. صيانة العشرة لأصحاب الفضل .. #نبي_الإحسان", "T1w2TdmQCYs", "37:58", "2025-03-15", "1029515", 37),
        EpisodeRecord(16, "حلقة 16 - الخلطة النبوية لإمتلاك الصلابة النفسية #نبي_الإحسان", "p4fexvfrSpM", "43:52", "2025-03-16", "1029516", 33),
        EpisodeRecord(17, "حلقة 17 - تعلم الصبر في الحياة من حبيبك رسول اللهﷺ #نبي_الإحسان", "oMIDLd-2_PY", "35:47", "2025-03-17", "1029517", 31),
        EpisodeRecord(18, "حلقة 18 - اوعى تكون بطلت تحب؟! أفضل طريقة لملء خزان الحب #نبي_الإحسان", "9Ikr2klWqcs", "38:54", "2025-03-18", "1029518", 28),
        EpisodeRecord(19, "حلقة 19 - ازاي تحول أذية الناس لمكسب كبير في الحياة ؟! #نبي_الإحسان", "-IsxNieh8QU", "43:57", "2025-03-19", "1029560", 25),
        EpisodeRecord(20, "حلقة 20- 5 شفاعات كبرى للنبي تنجيك من أهوال يوم القيامة (يوم التناد) #نبي_الإحسان", "RXV--P-GRbg", "39:04", "2025-03-20", "1029561", 22),
        EpisodeRecord(21, "حلقة 21- منهك نفسيًا؟؟ طريقة مجربة تحميك من الإحتراق الداخلي #نبي_الإحسان", "6wb1W1vQG_0", "36:23", "2025-03-21", "1029562", 19),
        EpisodeRecord(22, "حلقة 22 - الإعانة الربانية..طريقك لتحقيق المستحيلات بأقل الامكانيات #نبي_الإحسان", "qrwaTshvlXQ", "34:59", "2025-03-22", "1029563", 16),
        EpisodeRecord(23, "حلقة 23- شغلك عند ربنا سعادة الدنيا والآخرة.. بس اعرف الطريقة؟ .. #نبي_الإحسان", "is_w18cH_nI", "37:09", "2025-03-23", "1029564", 13),
        EpisodeRecord(24, "حلقة 24 - أنت موعود بالفتح مهما طال الانتظار.. #نبي_الإحسان", "ImUaE5c9ev8", "41:29", "2025-03-24", "1029565", 10),
        EpisodeRecord(25, "حلقة 25 - وسط ضغوط الحياة..هتختار إيه؟ #نبي_الإحسان", "IuuDzViGr-g", "31:32", "2025-03-25", "1029566", 7),
        EpisodeRecord(26, "حلقة 26 - ليلة القدر .. ليلة استجابة الدعاء والمغفرة والأرزاق.. #نبي_الإحسان", "VOkYAydi-t4", "34:12", "2025-03-26", "1029567", 6),
        EpisodeRecord(27, "حلقة 27 - عيشها بدون تعقيد.. أسهل طريقة للهدوء النفسي وراحة البال ..#نبي_الإحسان", "eH35xcP0GUE", "23:28", "2025-03-27", "1029568", 3),
        EpisodeRecord(28, "حلقة 28 - وفاة النبيﷺ أصعب اللحظات وأهم الوصايا \"طبت حيًا وميتًا يا رسول الله\" ..#نبي_الإحسان", "7AfOskqadEc", "40:21", "2025-03-28", "1029569", 2),
        EpisodeRecord(29, "حلقة 29 - حلقة الختام..أجواء ومشاعر رائعة.. كل عام وأنتم بخير .. #نبي_الإحسان", "NtZupF-OFYE", "33:35", "2025-03-29", "1029570", 1)
    )

    val defaultEpisodes: List<NabiIhsanEpisode> = records.map { record ->
        NabiIhsanEpisode(
            id = "ep_${record.number.toString().padStart(2, '0')}",
            episodeNumber = record.number,
            title = record.title,
            subtitle = "",
            aboutText = "",
            seerahContext = "",
            practicalReflection = "",
            source = PROGRAM_URL,
            youtubeVideoId = record.videoId,
            coverUrl = "https://i.ytimg.com/vi/${record.videoId}/hqdefault.jpg",
            duration = record.duration,
            publishedAt = record.publishedAt,
            sourceUrl = "https://amrkhaled.net/episode/${record.sourceEpisodeId}",
            playlistId = PLAYLIST_ID,
            playlistPosition = record.playlistPosition,
            sourceChannelId = OFFICIAL_CHANNEL_ID,
            isEmbeddable = true,
            topics = emptyList(),
            lessons = emptyList(),
            recipeIds = emptyList()
        )
    }

    // Official sources describe a 30-recipe concept but do not publish an itemized recipe catalog.
    // Unsupported fixture recipes and guessed episode relationships are intentionally omitted.
    val defaultRecipes: List<PropheticRecipe> = emptyList()
}
