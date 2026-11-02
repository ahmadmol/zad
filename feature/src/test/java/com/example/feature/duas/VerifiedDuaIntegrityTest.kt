package com.example.feature.duas

import com.example.feature.duas.presentation.DuaSections
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class VerifiedDuaIntegrityTest {
    @Test fun `all sections contain sourced whole texts with no generated religious prose`() {
        val base = listOf(File("src/main/assets"),File("feature/src/main/assets")).first { it.exists() }
        fun read(name:String)=Json.parseToJsonElement(File(base,name).readText())
        val verses=read("quran_full.json").jsonArray.flatMap { s -> s.jsonObject.getValue("verses").jsonArray.map { it.jsonObject.getValue("text").jsonPrimitive.content } }.toSet()
        val hadith=read("hadeethenc_ar_v1.7.0.json").jsonObject.getValue("records").jsonArray
            .associate { it.jsonObject.getValue("link").jsonPrimitive.content to it.jsonObject.getValue("hadith_text").jsonPrimitive.content }
        val entries=read("verified_duas.json").jsonArray.map { it.jsonObject }
        assertEquals(39,entries.size)
        assertEquals(39,entries.map { it.getValue("id").jsonPrimitive.long }.toSet().size)
        DuaSections.titles.forEach { section -> assertTrue(section,entries.any { DuaSections.matches(section,it.getValue("category").jsonPrimitive.content) }) }
        entries.forEach { item ->
            val text=item.getValue("text").jsonPrimitive.content
            val reference=item.getValue("reference").jsonPrimitive.content
            if(item.getValue("source").jsonPrimitive.content.startsWith("Tanzil")) {
                assertTrue(text in verses)
                assertTrue(item.getValue("copyright").jsonPrimitive.content.contains("Tanzil"))
            } else assertEquals(reference,hadith[reference],text)
            assertTrue(item.getValue("whenToSay").jsonPrimitive.content.isEmpty())
        }
    }
}
