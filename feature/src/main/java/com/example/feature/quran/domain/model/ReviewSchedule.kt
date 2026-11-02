package com.example.feature.quran.domain.model

enum class ReviewResult(val label: String) { AGAIN("أحتاج إلى إعادة"), HARD("بصعوبة"), GOOD("تذكّرت جيدًا") }
data class NextReview(val stage: Int, val dueDay: Long)

/** Civil days, no audio/reading inference. One recorded review per day; overdue starts today. */
object ReviewSchedule {
    val intervals = listOf(1, 3, 7, 14, 30)
    fun next(stage: Int, result: ReviewResult, today: Long): NextReview {
        require(stage in intervals.indices)
        val nextStage = when (result) {
            ReviewResult.AGAIN -> 0
            ReviewResult.HARD -> stage
            ReviewResult.GOOD -> (stage + 1).coerceAtMost(intervals.lastIndex)
        }
        val days = if (result == ReviewResult.HARD) 1 else intervals[nextStage]
        return NextReview(nextStage, today + days)
    }
}
