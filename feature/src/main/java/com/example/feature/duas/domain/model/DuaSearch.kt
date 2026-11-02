package com.example.feature.duas.domain.model

import com.example.feature.core.search.ArabicSearch

object DuaSearch {
    fun matches(title: String, text: String, category: String, query: String): Boolean {
        if (query.isBlank()) return true
        val key = ArabicSearch.key(query)
            .removePrefix("دعاء ").removePrefix("ادعية ").removePrefix("ادعيه ")
        return key.isNotBlank() && ArabicSearch.matches("$title $text $category", key)
    }
}
