package com.example.feature.duas.presentation

/** Display taxonomy maps explicit existing categories; it never infers religious sources. */
object DuaSections {
    val titles = listOf("أدعية الصباح", "أدعية المساء", "أدعية من القرآن الكريم", "أدعية نبوية",
        "أدعية للأهل والأصدقاء", "أدعية في الشدائد", "أدعية متنوعة")
    private val categories = mapOf(
        titles[0] to setOf("الصباح", "أدعية الصباح"),
        titles[1] to setOf("المساء", "أدعية المساء"),
        titles[2] to setOf("أدعية قرآنية", "أدعية من القرآن", "من القرآن الكريم"),
        titles[3] to setOf("من السنة النبوية", "أدعية من السنة", "أدعية نبوية"),
        titles[4] to setOf("الأهل والأصدقاء", "أدعية للأهل والأصدقاء"),
        titles[5] to setOf("الهم والحزن", "الشدائد", "أدعية في الشدائد"),
        titles[6] to setOf("السفر", "الرزق", "أدعية متنوعة")
    )
    fun matches(section: String, category: String): Boolean = categories[section]?.contains(category) ?: (section == category)
}
