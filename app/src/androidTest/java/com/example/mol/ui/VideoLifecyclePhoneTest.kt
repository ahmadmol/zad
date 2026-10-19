package com.example.mol.ui

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.learning.presentation.components.IhsanYouTubePlayer
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Native user taps and lifecycle transitions on the authorized physical QA package only. */
class VideoLifecyclePhoneTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private lateinit var activity: ComponentActivity
    private fun find(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) find(view.getChildAt(i))?.let { return it }
        return null
    }
    private fun shell(command: String) = android.os.ParcelFileDescriptor.AutoCloseInputStream(
        inst.uiAutomation.executeShellCommand(command)).use { it.readBytes() }
    private fun js(web: WebView, code: String): String {
        val latch = CountDownLatch(1)
        var result = ""
        inst.runOnMainSync { web.evaluateJavascript(code) { result = it; latch.countDown() } }
        check(latch.await(8, TimeUnit.SECONDS))
        return result
    }
    private fun tap(x: Float, y: Float) {
        val now = SystemClock.uptimeMillis()
        listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP).forEach { action ->
            val event = MotionEvent.obtain(now, now + if (action == MotionEvent.ACTION_UP) 80 else 0, action, x, y, 0)
            try { inst.sendPointerSync(event) } finally { event.recycle() }
        }
    }
    @Test fun fullscreenAndBackground() {
        check(android.os.Build.MODEL == "23129RAA4G" && inst.targetContext.packageName == "com.example.mol.referenceqa")
        shell("am start -W -n com.example.mol.referenceqa/androidx.activity.ComponentActivity")
        var resumed: ComponentActivity? = null
        val deadline = SystemClock.elapsedRealtime() + 15000
        while (resumed == null && SystemClock.elapsedRealtime() < deadline) {
            inst.runOnMainSync { resumed = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).filterIsInstance<ComponentActivity>().singleOrNull() }
            if (resumed == null) SystemClock.sleep(200)
        }
        activity = checkNotNull(resumed)
        inst.runOnMainSync {
            activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.setContent { IhsanTheme(darkTheme = true) { IhsanYouTubePlayer("Kx9Wj4vP69A", null, true, true, onOpenExternal = {}) } }
        }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("تشغيل الفيديو").performClick()
        compose.waitUntil(40000) { compose.onAllNodes(hasStateDescription("تشغيل الفيديو مؤكد")).fetchSemanticsNodes().isNotEmpty() }
        val web = checkNotNull(find(activity.window.decorView))
        val output = File(inst.targetContext.getExternalFilesDir(null), "physical-media-${System.currentTimeMillis()}").apply { mkdirs() }
        inst.sendStatus(0, android.os.Bundle().apply { putString("physicalMediaOutput", output.absolutePath) })
        fun capture(label: String) {
            File(output, "$label.json").writeText(js(web, """(function(){var r=player.getIframe().getBoundingClientRect();return {id:player.getVideoData().video_id,time:player.getCurrentTime(),state:player.getPlayerState(),muted:player.isMuted(),volume:player.getVolume(),rect:[r.x,r.y,r.width,r.height],fullscreen:!!document.fullscreenElement,visibility:document.visibilityState}})()"""))
            val bitmap = checkNotNull(inst.uiAutomation.takeScreenshot())
            File(output, "$label.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }; bitmap.recycle()
        }
        SystemClock.sleep(500)
        capture("inline")
        val pos = IntArray(2)
        var w = 0; var h = 0
        inst.runOnMainSync { web.getLocationOnScreen(pos); w = web.width; h = web.height }
        // Use the official accessible control; reveal it with a real native tap if hidden.
        fun fullscreenNode(): android.view.accessibility.AccessibilityNodeInfo? {
            fun search(node: android.view.accessibility.AccessibilityNodeInfo?): android.view.accessibility.AccessibilityNodeInfo? {
                if (node == null) return null
                val label = "${node.text?.toString().orEmpty()} ${node.contentDescription?.toString().orEmpty()}"
                if (label.contains("full screen", true) || label.contains("ملء الشاشة")) return node
                for (i in 0 until node.childCount) search(node.getChild(i))?.let { return it }
                return null
            }
            return search(inst.uiAutomation.rootInActiveWindow)
        }
        val node = fullscreenNode()
        File(output, "fullscreen-control.txt").writeText(node.toString())
        if (node != null) check(node.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK))
        else tap(pos[0] + w * .88f, pos[1] + h * .53f)
        SystemClock.sleep(2500)
        capture("fullscreen-request")
        // Entering fullscreen must preserve playback without a second play gesture.
        SystemClock.sleep(3000)
        capture("fullscreen-playing")
        SystemClock.sleep(2000)
        capture("fullscreen-progress")
        check(JSONObject(js(web, "({state:player.getPlayerState(),fullscreen:!!document.fullscreenElement})")).let {
            it.getInt("state") == 1 && it.getBoolean("fullscreen")
        }) { "Fullscreen must keep the existing video playing" }
        shell("input keyevent 4")
        SystemClock.sleep(1500)
        capture("after-fullscreen-back")
        shell("input keyevent 3")
        SystemClock.sleep(2000)
        capture("background-first")
        SystemClock.sleep(2000)
        capture("background-second")
        shell("am start -W --activity-reorder-to-front -n com.example.mol.referenceqa/androidx.activity.ComponentActivity")
        SystemClock.sleep(1500)
        capture("resumed-paused")
        // Explicit playback after return is intentional; no background autoplay required.
        js(web, "player.playVideo()")
        SystemClock.sleep(3000)
        capture("resumed-playing")
    }
}
