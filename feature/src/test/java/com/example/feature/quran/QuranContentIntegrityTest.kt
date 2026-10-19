package com.example.feature.quran

import com.example.feature.quran.domain.model.QuranNavigation
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class QuranContentIntegrityTest {
    @Test fun `every bundled Quran character equals Tanzil Uthmani 1_1 original XML`() {
        val base = listOf(File("src/main/assets"), File("feature/src/main/assets")).first { it.exists() }
        val xml = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(base, "licenses/tanzil-uthmani-1.1.xml"))
        val chapters = xml.getElementsByTagName("sura")
        val bundled = asset("quran_full.json").jsonArray
        assertEquals(114, chapters.length)
        bundled.forEachIndexed { i, chapter ->
            val original = (chapters.item(i) as org.w3c.dom.Element).getElementsByTagName("aya")
            val verses = chapter.jsonObject.getValue("verses").jsonArray
            assertEquals(original.length, verses.size)
            verses.forEachIndexed { n, verse ->
                assertEquals("${i+1}:${n+1}", (original.item(n) as org.w3c.dom.Element).getAttribute("text"),
                    verse.jsonObject.getValue("text").jsonPrimitive.content)
            }
            assertTrue(chapter.jsonObject.getValue("copyright").jsonPrimitive.content.contains("Tanzil"))
        }
    }
    private fun asset(name: String): JsonElement {
        val base = listOf(File("src/main/assets"), File("feature/src/main/assets")).first { it.exists() }
        return Json.parseToJsonElement(File(base, name).readText())
    }
    @Test fun `all 6236 unique verse keys belong to their 114 chapters with consecutive numbering`() {
        val surahs = asset("quran_full.json").jsonArray
        assertEquals(114, surahs.size)
        val keys = surahs.flatMapIndexed { index, s ->
            val chapter = s.jsonObject
            assertEquals(index + 1, chapter.getValue("id").jsonPrimitive.int)
            val verses = chapter.getValue("verses").jsonArray
            assertEquals(chapter.getValue("total_verses").jsonPrimitive.int, verses.size)
            verses.mapIndexed { n, v ->
                val verse = v.jsonObject
                assertEquals(n + 1, verse.getValue("id").jsonPrimitive.int)
                assertTrue(verse.getValue("text").jsonPrimitive.content.isNotBlank())
                (index + 1) to (n + 1)
            }
        }
        assertEquals(6236, keys.size)
        assertEquals(keys.size, keys.toSet().size)
    }
    @Test fun `604 page partition and 30 juz anchors cover whole Quran once in order`() {
        val keys = asset("quran_full.json").jsonArray.flatMap { s ->
            val chapter = s.jsonObject
            chapter.getValue("verses").jsonArray.map { chapter.getValue("id").jsonPrimitive.int to it.jsonObject.getValue("id").jsonPrimitive.int }
        }
        val pages = asset("mushaf_page_starts.json").jsonArray.map { it.jsonArray.let { p -> p[0].jsonPrimitive.int to p[1].jsonPrimitive.int } }
        assertEquals(604, pages.size)
        val offsets = pages.map(keys::indexOf) + keys.size
        assertEquals(0, offsets.first())
        assertTrue(offsets.zipWithNext().all { (a,b) -> a >= 0 && b > a })
        assertEquals(keys, offsets.zipWithNext().flatMap { (a,b) -> keys.subList(a,b) })
        val juz = asset("quran_juz_starts.json").jsonArray.map { it.jsonArray.let { p -> p[0].jsonPrimitive.int to p[1].jsonPrimitive.int } }
        assertEquals(juz, QuranNavigation.juzStarts)
        assertTrue(juz.map(keys::indexOf).zipWithNext().all { (a,b) -> a >= 0 && b > a })
        assertEquals(78 to 1, juz.last())
    }
}
