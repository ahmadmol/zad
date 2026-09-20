package com.example.feature.fahmanallah.data.datasource

import com.example.feature.fahmanallah.domain.model.FahmEpisode
import com.example.feature.fahmanallah.domain.model.FahmEpisodeProgressState
import com.example.feature.fahmanallah.domain.model.FahmStation

object FahmFixtureData {
    const val PROGRAM_URL = "https://amrkhaled.net/program/1148/الفهم-عن-الله-الجزء-الأول---رمضان-2023"
    const val PLAYLIST_ID = "PLhbs8A5De9zQQ2RjNZvIMEQ_JWYaBGP8e"
    const val OFFICIAL_CHANNEL_ID = "UCjxcF4A7pCyWsuJcZOymF_g"

    val STATIONS: List<FahmStation> = listOf(
        FahmStation("st_taqwa", "التقوى", "", "leaf", emptyList()),
        FahmStation("st_yaqeen", "اليقين", "", "lantern", emptyList()),
        FahmStation("st_tawakkul", "التوكل", "", "hands", emptyList()),
        FahmStation("st_tasleem", "التسليم", "", "heart_shield", emptyList()),
        FahmStation("st_reda", "الرضا", "", "star", emptyList()),
        FahmStation("st_ubudiyyah", "العبودية", "", "person_pray", emptyList()),
        FahmStation("st_mahabba", "محبة الله", "", "heart", emptyList())
    )

    private data class EpisodeRecord(
        val title: String,
        val videoId: String,
        val duration: String,
        val publishedAt: String
    )

    private val records = listOf(
        EpisodeRecord("الحلقة 1 | الفهم عن الله | ازاي تعدي بسلام من صدمات وأزمات الحياة؟ اطمئن", "VoHQtkOzwvU", "35:48", "2023-03-22"),
        EpisodeRecord("الحلقة 2 | الفهم عن الله | عايز أمانك ورزقك ؟ احذر الأمراض العشرة الفتاكة للتقوى", "f89uFne4br8", "35:26", "2023-03-24"),
        EpisodeRecord("الحلقة 3 | الفهم عن الله | 4 كلمات تسهل عليك الطريق لعبور الصراط يوم القيامة", "S0lPIdx2sy0", "36:37", "2023-03-25"),
        EpisodeRecord("الفهم عن الله | الحلقة 4 | احمي مثلث العلاقات الكبرى وحصنها بتقوى الله | عمرو خالد", "9ZtKCmePZTA", "35:40", "2023-03-26"),
        EpisodeRecord("الفهم عن الله | الحلقة 5 | استبشر.. وعد رباني عظيم لتجاوز الإحباط واليأس| عمرو خالد", "GNf2CknFU0A", "33:04", "2023-03-27"),
        EpisodeRecord("الفهم عن الله | الحلقة 6 | حط رجلك على أجمل طريق لتنهي معاناتك في الحياة", "GypVGtSU8WU", "32:43", "2023-03-28"),
        EpisodeRecord("الفهم عن الله | الحلقة 7 | اليقين في سعة قدّر الله = رزق واسع وفتح كبير", "PuIVd1VBLvw", "35:09", "2023-03-29"),
        EpisodeRecord("الفهم عن الله | الحلقة 8 | التوكل على الله..سندك وقوتك..ينصرك ويجبر بخاطرك فيما كسرك", "eV-FVl7eMBA", "34:10", "2023-03-30"),
        EpisodeRecord("الفهم عن الله | الحلقة 9 | إزاي تمشي على \"كتالوج ربنا\" صح؟ بحسن التوكل على الله | عمرو خالد", "dAx43bvNQ9E", "30:20", "2023-03-31"),
        EpisodeRecord("الفهم عن الله | الحلقة 10 | كن رجلا بألف رجل وسط ضغوط ومصائب الحياة بسر التوكل على الله", "CTmAa2Ay9IY", "35:09", "2023-04-01"),
        EpisodeRecord("الفهم عن الله |الحلقة 11|افهم عن الله..امتى يعطيك وامتى يمنعك!؟", "WL8vj8zd8RA", "35:30", "2023-04-02"),
        EpisodeRecord("الفهم عن الله|الحلقة 12|مع التسليم لله..(كل مر سيمر)أقوى الطرق لتخفيف همك وحزنك", "8nES5-BB68g", "36:11", "2023-04-03"),
        EpisodeRecord("الفهم عن الله | الحلقة13| لو الدنيا قهرتك وضلمت في وشك..واجه ألمك بمعادلة السعادة", "P8M8QLfW0u4", "38:23", "2023-04-04"),
        EpisodeRecord("الفهم عن الله|الحلقة 14|الوصول للرضا الداخلي في الزمن الصعب..طريقة سهلة لراحة النفس وروقان البال", "VgojMWbww1c", "35:09", "2023-04-05"),
        EpisodeRecord("الفهم عن الله|الحلقة 15|ازاي تحمي نفسك من التشتت بين الاختيارات الصعبة في الحياة", "5WMHtvTkaL8", "33:30", "2023-04-06"),
        EpisodeRecord("الفهم عن الله|الحلقة 16|5معاني تغرس الرضا في قلبك..هتحس بطعم السعادة الحقيقي", "y-mx8gqjuws", "32:26", "2023-04-07"),
        EpisodeRecord("الفهم عن الله|الحلقة 17|النية الحلوة طريقك للإخلاص وجلب التوفيق وفتح ابواب المدد الرباني", "JU0lg47NeKs", "33:19", "2023-04-08"),
        EpisodeRecord("الفهم عن الله|الحلقة 18|أحلى شغلة في الدنيا..إنك تشتغل عند ربنا..\"مقامك حيث أقامك", "4pQuXGBcD6M", "36:14", "2023-04-09"),
        EpisodeRecord("الفهم عن الله|الحلقة 19|فن الخلوة مع الله..أنا جايلك يارب..معاك لوحدي وبين يديك", "xlV5K7NtbkY", "36:09", "2023-04-10"),
        EpisodeRecord("الفهم عن الله|الحلقة 20|فكرة اليوم الذهبي تصل بك لليلة القدر..كما فعلها النبي ﷺ", "lpcMODg1Q1M", "30:34", "2023-04-11"),
        EpisodeRecord("الفهم عن الله|الحلقة 21|ازاي تعيش حياتك بأكثر صفة ربنا بيحبها فيك؟6 علامات تعرفك إن ربنا بيحبك", "TpOPriKrI0Y", "33:44", "2023-04-12"),
        EpisodeRecord("الفهم عن الله|الحلقة 22|طريقة ربانية ووصية نبوية للوصول لحب عميق لله في ليلة القدر", "Wn9U9pFPkVc", "30:53", "2023-04-13"),
        EpisodeRecord("الفهم عن الله|الحلقة 23|الحب \"مغناطيس الحياة \"خطة مجربة توصلك لأهدافك وأحلامك", "sRNMhAvtuGA", "27:06", "2023-04-14"),
        EpisodeRecord("الفهم عن الله|الحلقة 24| 5 بشريات ومحفزات من القرآن تملأك حماسًا لليلة القدر", "zECGBvClad0", "30:00", "2023-04-15"),
        EpisodeRecord("الفهم عن الله|الحلقة 25|تعلم فن الفضفضة..الباب الأكبر لتفريج الكروب والهموم والأوجاع", "PwiyjieTJ1I", "31:10", "2023-04-16"),
        EpisodeRecord("الفهم عن الله | الحلقة 26 | ليلة القدر..أعظم ليلة فى العمر.. ليلة استجابة الدعاء", "_KoW1Y8anEg", "28:19", "2023-04-17"),
        EpisodeRecord("الفهم عن الله|الحلقة 27|أجمل وأغلي نصيحة قبل وداع رمضان | عمرو خالد", "MoEF5APtIRA", "14:25", "2023-04-18"),
        EpisodeRecord("الفهم عن الله|الحلقة 28| أهم علامتين لقبول أعمالك واستجابة دعواتك في رمضان", "7MRpnyJ0mBg", "14:37", "2023-04-19"),
        EpisodeRecord("الفهم عن الله | الحلقة الأخيرة 29 | مسك ختام رحلة الفهم عن الله | عمرو خالد", "rd_53eZ-j8E", "17:23", "2023-04-20")
    )

    val OFFICIAL_TITLES: List<String> = records.map { it.title }

    val INITIAL_EPISODES: List<FahmEpisode> = records.mapIndexed { index, record ->
        val number = index + 1
        FahmEpisode(
            id = "ep_${number.toString().padStart(2, '0')}",
            number = number,
            title = record.title,
            videoId = record.videoId,
            durationText = record.duration,
            coverUrl = "https://i.ytimg.com/vi/${record.videoId}/hqdefault.jpg",
            publishedAt = record.publishedAt,
            sourceUrl = "https://amrkhaled.net/episode/${1028886 - number}",
            playlistId = PLAYLIST_ID,
            playlistPosition = number,
            sourceChannelId = OFFICIAL_CHANNEL_ID,
            isEmbeddable = true,
            stationId = null,
            progressState = FahmEpisodeProgressState.NOT_STARTED
        )
    }
}
