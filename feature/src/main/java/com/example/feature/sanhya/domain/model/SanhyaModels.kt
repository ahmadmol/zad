package com.example.feature.sanhya.domain.model

data class QuranStory(
    val id: String,
    val title: String,
    val subtitle: String,
    val summary: String,
    val coverUrl: String,
    val surahs: List<String>,
    val category: String,
    val quranReferences: List<QuranReference>,
    val lessons: List<StoryLesson>,
    val episodes: List<StoryEpisode>,
    val sourceTitle: String = "قصص القرآن - الجزء الأول",
    val sourceAuthor: String = "د. عمرو خالد",
    val keywords: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val isInWatchLater: Boolean = false,
    val lastWatchedEpisodeId: String? = null
) {
    val totalEpisodesCount: Int get() = episodes.size
    val formattedSurahLabel: String get() = when {
        surahs.isEmpty() -> ""
        surahs.size == 1 -> surahs.first()
        else -> "سور مختلفة"
    }
}

data class QuranReference(
    val surahId: Int,
    val surahName: String,
    val startAyah: Int,
    val endAyah: Int
) {
    val ayahRangeLabel: String get() = "سورة $surahName من الآية $startAyah إلى $endAyah"
}

data class StoryLesson(
    val id: String,
    val text: String
)

data class StoryEpisode(
    val id: String,
    val episodeNumber: Int? = null,
    val title: String,
    val partLabel: String? = null,
    val youtubeVideoId: String? = null,
    val durationLabel: String? = null,
    val thumbnailUrl: String? = null,
    val publishedAt: String? = null,
    val sourceUrl: String? = null,
    val playlistId: String? = null,
    val playlistPosition: Int? = null,
    val sourceChannelId: String? = null,
    val isEmbeddable: Boolean = false,
    val isAvailable: Boolean = true,
    val chapters: List<EpisodeChapter> = emptyList()
)

data class EpisodeChapter(
    val title: String,
    val startSeconds: Int
)

enum class StorySortOption(val label: String) {
    NEWEST_FIRST("الأحدث أولاً"),
    OLDEST_FIRST("الأقدم أولاً"),
    SERIES_ORDER("حسب ترتيب السلسلة")
}

enum class EpisodeCountFilter(val label: String) {
    ALL("الكل"),
    SINGLE_EPISODE("أكثر من حلقة"), // Match mockup label or single
    MULTI_EPISODE("أكثر من حلقة")
}

data class StoryFilterState(
    val selectedCategory: String = "الكل",
    val selectedEpisodeFilter: String = "الكل",
    val selectedDurationFilter: String = "الكل",
    val sortOption: StorySortOption = StorySortOption.NEWEST_FIRST
)
