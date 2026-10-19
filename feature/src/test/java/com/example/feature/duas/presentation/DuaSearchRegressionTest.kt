package com.example.feature.duas.presentation

import com.example.feature.duas.domain.model.DuaSearch
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class DuaSearchRegressionTest {
    @Test fun travelDuaIntentFindsOnlyTravelItemsInActualVerifiedEdition() {
        val base = listOf(File("src/main/assets"), File("feature/src/main/assets")).first { it.exists() }
        val rows = Json.parseToJsonElement(File(base, "verified_duas.json").readText()).jsonArray.map { it.jsonObject }
        val matches = rows.filter { DuaSearch.matches(it.getValue("title").jsonPrimitive.content,
            it.getValue("text").jsonPrimitive.content, it.getValue("category").jsonPrimitive.content, "دعاء السفر") }
        assertEquals(2, matches.size)
        assertTrue(matches.all { it.getValue("category").jsonPrimitive.content == "السفر" })
        assertFalse(DuaSearch.matches("سيد الاستغفار", "نص آخر", "أدعية المساء", "دعاء السفر"))
        assertTrue(DuaSearch.matches("سيد الاستغفار", "نص آخر", "أدعية المساء", "سيد الاستغفار"))
    }
}
