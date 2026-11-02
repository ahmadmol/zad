package com.example.feature.learning
import com.example.feature.learning.presentation.components.YouTubeNavigationPolicy
import org.junit.Assert.*
import org.junit.Test
class YouTubeNavigationPolicyTest {
 @Test fun `only the four intended playback callbacks are exposed to JavaScript`() {
  val exposed=com.example.feature.learning.presentation.components.YouTubePlaybackBridge::class.java.declaredMethods
   .filter {it.getAnnotation(android.webkit.JavascriptInterface::class.java)!=null}.map {it.name}.toSet()
  assertEquals(setOf("ready","playing","stopped","failed"),exposed)
 }
 @Test fun `official embed root and blank remain usable`() {
  listOf("about:blank","https://www.youtube.com/","https://youtube.com:443/embed/abcdefghijk?start=30").forEach { assertTrue(it,YouTubeNavigationPolicy.allowsMainFrame(it)) }
 }
 @Test fun `untrusted credentials ports schemes and encoded paths are blocked`() {
  listOf("https://youtube.com.evil.test/embed/abcdefghijk","https://evil@youtube.com/embed/abcdefghijk","https://youtube.com:444/embed/abcdefghijk",
   "http://youtube.com/","file:///data/data/private","content://private","javascript:alert(1)","https://youtube.com/%65mbed/abcdefghijk",
   "https://youtube.com/embed/../watch","https://youtube.com/watch?v=abcdefghijk","https://youtube.com/embed/a%27script").forEach { assertFalse(it,YouTubeNavigationPolicy.allowsMainFrame(it)) }
 }
}
