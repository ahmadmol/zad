package com.example.feature.batch05
import com.example.feature.core.search.ArabicSearch
import com.example.feature.hadith.domain.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import java.io.File
import java.security.MessageDigest
import org.junit.Assert.*
import org.junit.Test
class Batch05SearchMeasurementTest {
 @Test fun `prepared search preserves edition ordering and measures host-only cold warm cost`()=runBlocking {
  val root=generateSequence(File(".").canonicalFile) { it.parentFile }.first { File(it,"settings.gradle.kts").exists() }
  val asset=File(root,"feature/src/main/assets/hadeethenc_ar_v1.7.0.json")
  val raw=asset.readText();val corpus=Json.parseToJsonElement(raw).jsonObject["records"]!!.jsonArray
  val rows=corpus.map { record ->
   val r=record.jsonObject;fun str(k:String)=r[k]!!.jsonPrimitive.content
   Hadith(id=100000000L+str("id").toLong(),text=str("hadith_text"),narrator="مذكور في النص الأصلي",source="HadeethEnc.com — v1.7.0\n${str("takhrij")}\nالحكم كما في المصدر: ${str("grade")}\n${str("link")}",category="موسوعة الأحاديث")
  }
  assertEquals(3582,rows.size)
  fun legacy(query:String)=rows.filter { row -> listOf(row.text,row.narrator,row.source,row.category).any { ArabicSearch.matches(it,query) } }
  val queries=listOf("الصلاة","رحمة","عبارةاختبارغيرموجودة","الله","صَلَاة","موسوعة")
  val index=HadithSearchIndex();val coldStart=System.nanoTime();index.filter(rows,queries[0]);val cold=(System.nanoTime()-coldStart)/1e6
  repeat(3) { index.filter(rows,queries[it]);legacy(queries[it]) }
  val before=mutableListOf<Double>();val after=mutableListOf<Double>()
  repeat(10) { n ->
   val query=queries[n%queries.size]
   var start=System.nanoTime();val expected=legacy(query);before+=(System.nanoTime()-start)/1e6
   start=System.nanoTime();val actual=index.filter(rows,query);after+=(System.nanoTime()-start)/1e6
   assertEquals(expected.map { it.id },actual.map { it.id })
  }
  val changed=rows.map { it.copy(isFavorite=!it.isFavorite) };assertEquals(legacy(queries[0]).map { it.id },index.filter(changed,queries[0]).map { it.id })
  assertTrue(index.filter(rows,"ـــ").isEmpty())
  fun metrics(values:List<Double>)=buildJsonObject { put("medianMs",values.sorted()[values.size/2]);put("p95Ms",values.sorted()[(values.size*0.95).toInt().coerceAtMost(values.lastIndex)]) }
  val result=buildJsonObject {
   put("scope","host JVM only, not Android/device/Release performance acceptance; no D08 budget inferred")
   put("samples",10);put("rows",rows.size);put("preparedCacheRowCap",4096);put("coldPrepareMs",cold)
   put("legacy",metrics(before));put("prepared",metrics(after));put("java",System.getProperty("java.version"))
   put("assetSha256",MessageDigest.getInstance("SHA-256").digest(asset.readBytes()).joinToString("") { "%02x".format(it) })
  }
  File(root,"feature/build/reports/batch05").apply { mkdirs() }.resolve("search-measurement.json").writeText(result.toString())
 }
}
