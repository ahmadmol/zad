package com.example.feature.batch05
import android.content.Context
import android.content.res.AssetManager
import com.example.feature.quran.data.local.QuranAssetLoader
import com.example.feature.quran.data.local.dao.QuranDao
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Test
import java.io.*
class Batch05QuranLoaderTest {
 @Test fun `missing metadata fails visibly and never writes any canonical rows`()=runTest {
  val context=mockk<Context>();val assets=mockk<AssetManager>();every { context.assets } returns assets
  every { assets.open(any()) } throws IOException("private path")
  val dao=mockk<QuranDao>();coEvery { dao.countSurahs() } returns 0
  try { QuranAssetLoader(context,dao).loadIfNeeded();fail() } catch(_:IOException) { }
  coVerify(exactly=0) { dao.applyCanonicalContent(any(),any()) }
 }
 @Test fun `invalid full edition is rejected before writing metadata`()=runTest {
  val context=mockk<Context>();val assets=mockk<AssetManager>();every { context.assets } returns assets
  every { assets.open("quran.json") } returns ByteArrayInputStream("{\"surahs\":[]}".toByteArray())
  every { assets.open("quran_full.json") } returns ByteArrayInputStream("[]".toByteArray())
  val dao=mockk<QuranDao>();coEvery { dao.countSurahs() } returns 0
  try { QuranAssetLoader(context,dao).loadIfNeeded();fail() } catch(_:IllegalArgumentException) { }
  coVerify(exactly=0) { dao.applyCanonicalContent(any(),any()) }
 }
 @Test fun `cancellation never becomes a successful empty load`()=runTest {
  val context=mockk<Context>();val assets=mockk<AssetManager>();every { context.assets } returns assets
  every { assets.open(any()) } throws CancellationException()
  val dao=mockk<QuranDao>();coEvery { dao.countSurahs() } returns 0
  try { QuranAssetLoader(context,dao).loadIfNeeded();fail() } catch(_:CancellationException) { }
 }
 @Test fun `real bundled edition loads off caller thread and warm reopen does not parse again`()=runTest {
  val root=generateSequence(File(".").canonicalFile) { it.parentFile }.first { File(it,"settings.gradle.kts").exists() }
  val context=mockk<Context>();val assets=mockk<AssetManager>();every { context.assets } returns assets
  val caller=Thread.currentThread().name;var sourceThread=""
  every { assets.open(any()) } answers { sourceThread=Thread.currentThread().name;File(root,"feature/src/main/assets/${firstArg<String>()}").inputStream() }
  val dao=mockk<QuranDao>();var loaded=false
  coEvery { dao.countSurahs() } answers { if(loaded) 114 else 0 };coEvery { dao.countAyahs() } answers { if(loaded) 6236 else 0 }
  coEvery { dao.applyCanonicalContent(any(),any()) } coAnswers {
   assertEquals(114,firstArg<List<Any>>().size);assertEquals(6236,secondArg<List<Any>>().size);loaded=true
  }
  val loader=QuranAssetLoader(context,dao);loader.loadIfNeeded();loader.loadIfNeeded()
  assertNotEquals(caller,sourceThread);verify(exactly=2) { assets.open(any()) };coVerify(exactly=1) { dao.applyCanonicalContent(any(),any()) }
 }
}
