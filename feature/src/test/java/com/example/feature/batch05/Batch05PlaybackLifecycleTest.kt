package com.example.feature.batch05
import android.os.Handler
import android.os.Looper
import com.example.feature.learning.presentation.components.YouTubePlaybackBridge
import com.example.feature.learning.presentation.components.YouTubeNavigationPolicy
import io.mockk.*
import org.junit.Assert.*
import org.junit.Test
class Batch05PlaybackLifecycleTest {
 @Test fun `queued playback callback cannot mutate disposed player`() {
  mockkStatic(Looper::class);mockkConstructor(Handler::class)
  try {
   every { Looper.getMainLooper() } returns mockk<Looper>()
   val queue=mutableListOf<Runnable>()
   every { anyConstructed<Handler>().post(any()) } answers { queue+=firstArg<Runnable>();true }
   every { anyConstructed<Handler>().removeCallbacksAndMessages(null) } just Runs
   var plays=0;val bridge=YouTubePlaybackBridge({}, { plays++ }, {})
   bridge.playing();assertEquals(1,queue.size);bridge.close();queue.forEach { it.run() };bridge.playing()
   assertEquals(0,plays);assertEquals(1,queue.size)
   verify(exactly=1) { anyConstructed<Handler>().removeCallbacksAndMessages(null) }
  } finally { unmockkConstructor(Handler::class);unmockkStatic(Looper::class) }
 }
 @Test fun `external player URLs require trusted https origin and standard port`() {
  assertTrue(YouTubeNavigationPolicy.allowsExternal("https://www.youtube.com/watch?v=abcdefghijk"))
  listOf("https://evil@youtube.com/watch?v=x","https://youtube.com:444/watch?v=x","https://youtube.com.evil.test/watch","javascript:alert(1)","file:///private").forEach {
   assertFalse(it,YouTubeNavigationPolicy.allowsExternal(it))
  }
 }
}
