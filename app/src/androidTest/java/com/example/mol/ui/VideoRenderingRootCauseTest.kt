package com.example.mol.ui

import android.graphics.Bitmap
import android.os.SystemClock
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.learning.presentation.components.IhsanYouTubePlayer
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test

/** Diagnostic A/B on a physical phone. Never edits user preferences or media security policy. */
class VideoRenderingRootCauseTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private lateinit var activity: ComponentActivity
    private fun find(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) find(view.getChildAt(i))?.let { return it }
        return null
    }
    private fun js(web: WebView, code: String): String {
        val done = CountDownLatch(1)
        var result = ""
        inst.runOnMainSync { web.evaluateJavascript(code) { result = it; done.countDown() } }
        check(done.await(8, TimeUnit.SECONDS))
        return result
    }
    @Test fun diagnoseOriginalThenNativeLayoutParameters() {
        check(android.os.Build.MODEL == "23129RAA4G")
        check(inst.targetContext.packageName == "com.example.mol.referenceqa")
        android.os.ParcelFileDescriptor.AutoCloseInputStream(inst.uiAutomation.executeShellCommand(
            "am start -W -n com.example.mol.referenceqa/androidx.activity.ComponentActivity"
        )).use { it.readBytes() }
        val deadline = SystemClock.elapsedRealtime() + 15000
        var resumed: ComponentActivity? = null
        while (resumed == null && SystemClock.elapsedRealtime() < deadline) {
            inst.runOnMainSync { resumed = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED).filterIsInstance<ComponentActivity>().singleOrNull() }
            if (resumed == null) SystemClock.sleep(200)
        }
        activity = checkNotNull(resumed)
        inst.runOnMainSync {
            activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            activity.setContent { IhsanTheme { IhsanYouTubePlayer("Kx9Wj4vP69A", null, true, true, onOpenExternal = {}) } }
        }
        compose.waitForIdle()
        compose.onNodeWithContentDescription("تشغيل الفيديو").performClick()
        compose.waitUntil(40000) { compose.onAllNodes(hasStateDescription("تشغيل الفيديو مؤكد")).fetchSemanticsNodes().isNotEmpty() }
        val web = checkNotNull(find(activity.window.decorView))
        val output = File(inst.targetContext.getExternalFilesDir(null), "video-rca-${System.currentTimeMillis()}").apply { mkdirs() }
        fun capture(label: String) {
            SystemClock.sleep(3000)
            val report = JSONObject()
            inst.runOnMainSync {
                val position = IntArray(2); web.getLocationOnScreen(position)
                report.put("nativeWidth", web.width).put("nativeHeight", web.height).put("x", position[0]).put("y", position[1])
                    .put("shown", web.isShown).put("visibility", web.visibility).put("alpha", web.alpha)
                    .put("hardwareAccelerated", web.isHardwareAccelerated).put("layerType", web.layerType)
                    .put("layoutWidth", web.layoutParams?.width).put("layoutHeight", web.layoutParams?.height)
                    .put("windowVisibility", web.windowVisibility).put("windowFocus", activity.hasWindowFocus())
            }
            report.put("dom", js(web, """(function(){var f=player.getIframe(),r=f.getBoundingClientRect(),s=getComputedStyle(f);return {viewport:[innerWidth,innerHeight],body:[document.body.clientWidth,document.body.clientHeight],rect:[r.x,r.y,r.width,r.height],display:s.display,visibility:s.visibility,opacity:s.opacity,documentVisible:document.visibilityState,video:player.getVideoData(),state:player.getPlayerState(),time:player.getCurrentTime()}})()"""))
            File(output, "$label.json").writeText(report.toString(2))
            val bitmap = checkNotNull(inst.uiAutomation.takeScreenshot())
            File(output, "$label.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }; bitmap.recycle()
        }
        // Recreate the pre-fix intrinsic sizing to keep the A/B reproducible after the fix.
        inst.runOnMainSync { web.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT); web.requestLayout() }
        capture("original")
        inst.runOnMainSync { web.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT); web.requestLayout() }
        capture("explicit-match-parent")
        inst.runOnMainSync { web.setBackgroundColor(android.graphics.Color.BLACK) }
        capture("explicit-background")
        inst.sendStatus(0, android.os.Bundle().apply { putString("rcaOutput", output.absolutePath) })
    }
}
