package com.example.feature.duas

import com.example.feature.duas.domain.model.Dua
import org.junit.Test
import org.junit.Assert.*

class DuaPresentationMetadataTest {
    private val dua=Dua(1,"رزق وسفر","fixture canonical text","fixture","fixture","fixture")
    @Test fun `missing metadata never infers timing or religious explanation from matching title words`() {
        assertEquals("لم يتوفر شرح معتمد لهذا الدعاء في البيانات الحالية.",getDuaExplanation(dua))
        assertEquals("لم يتوفر وقت أو موضع موثق لهذا الدعاء في البيانات الحالية.",getDuaWhenToSay(dua))
        assertEquals(getDuaExplanation(dua),getDuaExplanation(dua.copy(title="الهم",text="أستخيرك")))
    }
    @Test fun `provided metadata is displayed verbatim without rewriting the approved text`() {
        val approved=dua.copy(explanation="fixture verified explanation",whenToSay="fixture verified timing")
        assertEquals(approved.explanation,getDuaExplanation(approved))
        assertEquals(approved.whenToSay,getDuaWhenToSay(approved))
        assertEquals(dua.text,approved.text)
    }
}
