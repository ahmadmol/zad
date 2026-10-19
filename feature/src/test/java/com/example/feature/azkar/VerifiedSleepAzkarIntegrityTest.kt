package com.example.feature.azkar

import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VerifiedSleepAzkarIntegrityTest {
    @Test fun fullSleepRecordsEqualPublisherCorpusWithoutReligiousTextEdits() {
        val base = listOf(File("src/main/assets"), File("feature/src/main/assets")).first { it.exists() }
        val corpus = Json.parseToJsonElement(File(base, "hadeethenc_ar_v1.7.0.json").readText()).jsonObject
        val records = corpus.getValue("records").jsonArray.associateBy { it.jsonObject.getValue("id").jsonPrimitive.content }
        val rows = Json.parseToJsonElement(File(base, "verified_sleep_azkar.json").readText()).jsonArray
        assertEquals(2, rows.size)
        rows.forEach { value ->
            val row = value.jsonObject
            val original = records.getValue(row.getValue("publisherId").jsonPrimitive.content).jsonObject
            assertEquals(original.getValue("hadith_text"), row.getValue("text"))
            assertEquals(corpus.getValue("publisher_notice"), row.getValue("publisher_notice"))
            assertTrue(row.getValue("source").jsonPrimitive.content.contains(original.getValue("link").jsonPrimitive.content))
            assertEquals(1, row.getValue("targetCount").jsonPrimitive.int)
        }
    }
}
