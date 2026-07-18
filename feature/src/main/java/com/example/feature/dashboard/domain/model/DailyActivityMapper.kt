package com.example.feature.dashboard.domain.model

import com.example.feature.core.preferences.DailyActivityIds

object DailyActivityMapper {
    private val templates = listOf(
        Template(DailyActivityIds.QURAN_READING, "قراءة قرآن", "مرة", "quran_list"),
        Template(DailyActivityIds.MORNING_AZKAR, "أذكار الصباح", "مرة", "azkar_screen"),
        Template(DailyActivityIds.EVENING_AZKAR, "أذكار المساء", "مرة", "azkar_screen"),
        Template(DailyActivityIds.TASBEEH, "تسبيح", "حبة", "tasbih_screen"),
        Template(DailyActivityIds.DAILY_DUA, "دعاء اليوم", "مرة", "dua_screen"),
        Template(DailyActivityIds.DAILY_NAME, "اسم اليوم", "مرة", "asma_screen")
    )

    fun mapItems(counts: Map<String, Int>): List<HomeDailyActivityItem> =
        templates.map { template ->
            val target = DailyActivityIds.targetFor(template.id)
            val current = (counts[template.id] ?: 0).coerceIn(0, target)
            HomeDailyActivityItem(
                id = template.id,
                title = template.title,
                currentCount = current,
                targetCount = target,
                unit = template.unit,
                isCompleted = current >= target,
                route = template.route
            )
        }

    fun isValidId(activityId: String): Boolean = activityId in DailyActivityIds.ALL

    private data class Template(
        val id: String,
        val title: String,
        val unit: String,
        val route: String
    )
}
