package com.example.mol.ui

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.learning.presentation.components.IhsanYouTubePlayer
import org.json.JSONObject
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Physical-device-only media probes. Does not modify user profiles, preferences or listings.
 * These ComponentActivity probes require the debug QA APK; they do not certify the release APK.
 * A successful JUnit run means the ledger was produced, not that all videos played.
 */
class PhysicalDeviceMediaAuditTest {
    private val compose = createEmptyComposeRule()
    private lateinit var activity: ComponentActivity
    private val inst get() = InstrumentationRegistry.getInstrumentation()
    private val deviceGuard = object : ExternalResource() {
        override fun before() {
            check(Build.MODEL == "23129RAA4G" && Build.MANUFACTURER.equals("Xiaomi", true))
            check(!Build.HARDWARE.contains("ranchu") && !Build.HARDWARE.contains("goldfish"))
            check(inst.targetContext.packageName == "com.example.mol.referenceqa")
            check(InstrumentationRegistry.getArguments().getString("physicalDeviceSerial") == "ac190123") {
                "Host must verify ac190123 using physical_device_preflight.ps1 before dispatch."
            }
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(deviceGuard).around(compose)

    private fun webView(view: View): WebView? {
        if (view is WebView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) webView(view.getChildAt(i))?.let { return it }
        return null
    }

    private fun playerSnapshot(): JSONObject? {
        val done = CountDownLatch(1)
        var result: JSONObject? = null
        inst.runOnMainSync {
            val playerView = webView(activity.window.decorView)
            if (playerView == null) done.countDown() else playerView.evaluateJavascript(
                """(function(){try { var d=player.getVideoData(),r=player.getIframe().getBoundingClientRect(); return {id:d.video_id,time:player.getCurrentTime(),state:player.getPlayerState(),muted:player.isMuted(),volume:player.getVolume(),rect:[r.x,r.y,r.width,r.height],visibility:document.visibilityState}; } catch(e) {return null;}})()"""
            ) { raw ->
                result = runCatching { JSONObject(raw) }.getOrNull()
                done.countDown()
            }
        }
        check(done.await(5, TimeUnit.SECONDS)) { "WebView inspection did not return" }
        return result
    }

    private fun capture(output: File, name: String) {
        compose.waitForIdle()
        check(activity.hasWindowFocus()) { "An external window obscures the media probe" }
        val bitmap = checkNotNull(inst.uiAutomation.takeScreenshot())
        try {
            File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } finally { bitmap.recycle() }
    }

    @Test fun all86VideosProduceIndividualPlaybackEvidence() {
        val smokeOnly = InstrumentationRegistry.getArguments().getString("smokeOnly") == "true"
        val ids = InstrumentationRegistry.getArguments().getString("videoIds").orEmpty().split(',')
        val collections = InstrumentationRegistry.getArguments().getString("videoCollections").orEmpty().split(',')
        check(collections.size == ids.size && collections.all { it in setOf("sanhya", "fahmanallah", "nabiihsan") })
        check((if (smokeOnly) ids.size in 1..85 else ids.size == 86) && ids.distinct().size == ids.size && ids.all { it.matches(Regex("[A-Za-z0-9_-]{11}")) }) {
            "Supply exactly the 86 IDs from the current source inventory."
        }
        // MIUI may deny ActivityScenario's background launch. Use the shell's explicit
        // foreground launch, without changing OEM permissions or touching app data.
        inst.uiAutomation.executeShellCommand("am start -W -n com.example.mol.referenceqa/androidx.activity.ComponentActivity").use {
            FileInputStream(it.fileDescriptor).use { output -> output.readBytes() }
        }
        var resumed: ComponentActivity? = null
        val launchDeadline = SystemClock.elapsedRealtime() + 15_000
        while (resumed == null && SystemClock.elapsedRealtime() < launchDeadline) {
            inst.runOnMainSync {
                resumed = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED)
                    .filterIsInstance<ComponentActivity>().singleOrNull()
            }
            if (resumed == null) SystemClock.sleep(200)
        }
        activity = checkNotNull(resumed) { "Physical probe activity could not be resumed; no video tested" }
        inst.runOnMainSync {
            activity.window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        val output = File(inst.targetContext.getExternalFilesDir(null), "physical-media-${System.currentTimeMillis()}").apply { mkdirs() }
        File(output, "device.json").writeText(JSONObject().put("model", Build.MODEL)
            .put("api", Build.VERSION.SDK_INT).put("package", inst.targetContext.packageName)
            .put("scope", if (smokeOnly) "DEBUG_SMOKE_NOT_86_VIDEO_ACCEPTANCE" else "DEBUG_QA_PLAYER_PROBE_NOT_RELEASE_ACCEPTANCE").toString(2))
        val ledger = File(output, "video-results.jsonl")
        val current = mutableStateOf(ids.first())
        val dark = mutableStateOf(false)
        var externalClicks = 0
        inst.runOnMainSync { activity.setContent { IhsanTheme(darkTheme = dark.value) { key(current.value) {
            // Exercise the same three production wrappers used by the learning screens.
            when (collections[ids.indexOf(current.value)]) {
                "fahmanallah" -> com.example.feature.fahmanallah.presentation.components.FahmVideoPlayer(current.value, null,
                    isEmbeddable = true, onOpenExternalYouTube = { externalClicks++ })
                "sanhya" -> com.example.feature.sanhya.presentation.components.QuranStoryVideoPlayer(current.value, null,
                    isEmbeddable = true, onOpenExternalYouTube = { externalClicks++ })
                else -> com.example.feature.nabiihsan.presentation.components.NabiIhsanVideoPlayer(current.value, null,
                    isEmbeddable = true, onOpenExternalYouTube = { externalClicks++ })
            }
        } } } }
        ids.forEach { id ->
            // Stop on loss of the test window instead of assigning failures to untouched videos.
            compose.waitForIdle()
            runCatching { compose.waitUntil(10_000) {
                var focused = false
                inst.runOnMainSync { focused = activity.hasWindowFocus() }
                focused
            } }
            var hasFocus = false
            inst.runOnMainSync { hasFocus = activity.hasWindowFocus() }
            check(hasFocus) { "Phone test interrupted by another window; remaining videos are NOT TESTED" }
            val row = JSONObject().put("videoId", id).put("startedAtEpochMs", System.currentTimeMillis())
                .put("status", "Not Tested").put("audioObserved", "Not Tested")
                .put("collection", collections[ids.indexOf(id)])
            Log.i("IhsanPhysicalQa", "VIDEO_START id=$id")
            try {
                compose.runOnIdle { current.value = id }
                compose.waitForIdle()
                compose.onNodeWithContentDescription("تشغيل الفيديو").performClick()
                var didPlay = false
                var fallback = false
                val observed = runCatching { compose.waitUntil(40_000) {
                    didPlay = compose.onAllNodes(hasStateDescription("تشغيل الفيديو مؤكد")).fetchSemanticsNodes().isNotEmpty()
                    fallback = compose.onAllNodesWithText("تعذر تشغيل الفيديو داخل التطبيق").fetchSemanticsNodes().isNotEmpty()
                    didPlay || fallback
                } }.isSuccess
                row.put("playingCallback", didPlay).put("fallbackVisible", fallback)
                if (didPlay) {
                    val before = playerSnapshot()
                    SystemClock.sleep(3_000)
                    val after = playerSnapshot()
                    row.put("before", before ?: JSONObject.NULL).put("after", after ?: JSONObject.NULL)
                    val sameVideo = before?.optString("id") == id && after?.optString("id") == id
                    val progress = sameVideo && after!!.optDouble("time", 0.0) > before!!.optDouble("time", 0.0) + 1.0
                    row.put("requestedIdentityConfirmed", sameVideo).put("progressConfirmed", progress)
                    row.put("status", if (progress) "Blocked" else "Fail")
                    row.put("reason", if (progress) "PENDING_SCREENSHOT_REVIEW_AND_AUDIBLE_AUDIO_CONFIRMATION" else "PLAYING_WITHOUT_VERIFIED_IDENTITY_OR_PROGRESS")
                } else if (observed && fallback) {
                    val count = externalClicks
                    compose.onNodeWithText("المشاهدة على YouTube").performClick()
                    check(externalClicks == count + 1)
                    row.put("status", "Blocked").put("reason", "EMBED_UNAVAILABLE_SEE_CORRELATED_LOGCAT")
                        .put("fallbackCallbackConfirmed", true).put("externalApplicationOpened", false)
                } else {
                    row.put("status", "Fail").put("reason", "NO_PLAYING_OR_FALLBACK_WITHIN_40_SECONDS")
                }
                capture(output, id)
                row.put("screenshot", "$id.png")
                compose.runOnIdle { dark.value = true }
                capture(output, "${id}_dark")
                row.put("darkScreenshot", "${id}_dark.png")
                compose.runOnIdle { dark.value = false }
            } catch (error: Exception) {
                row.put("status", "Fail").put("reason", error.toString())
            } catch (error: AssertionError) {
                row.put("status", "Fail").put("reason", error.toString())
            } finally {
                row.put("finishedAtEpochMs", System.currentTimeMillis())
                ledger.appendText(row.toString() + "\n")
                Log.i("IhsanPhysicalQa", "VIDEO_END id=$id status=${row.optString("status")}")
                inst.sendStatus(0, Bundle().apply {
                    putString("physicalMediaOutput", output.absolutePath)
                    putString("physicalMediaVideoId", id)
                    putString("physicalMediaStatus", row.optString("status"))
                })
            }
        }
        check(ledger.readLines().size == ids.size) { "Incomplete ledger; unrecorded videos are Not Tested" }
        if (smokeOnly) return
        val currentLive = mutableStateOf(com.example.feature.live.LiveSourceType.HARAM)
        inst.runOnMainSync { activity.setContent { IhsanTheme {
            key(currentLive.value) {
                com.example.feature.live.LiveStreamScreen(source = currentLive.value, onBack = {})
            }
        } } }
        val liveLedger = File(output, "live-results.jsonl")
        com.example.feature.live.LiveSourceType.entries.forEach { source ->
            val row = JSONObject().put("channelId", source.youtubeChannelId).put("source", source.id)
                .put("audioObserved", "PENDING_USER_ACTION").put("startedAtEpochMs", System.currentTimeMillis())
            try {
                compose.runOnIdle { currentLive.value = source }
                var played = false
                var fallback = false
                val observed = runCatching { compose.waitUntil(40_000) {
                    played = compose.onAllNodes(hasStateDescription("تشغيل البث مؤكد")).fetchSemanticsNodes().isNotEmpty()
                    fallback = compose.onAllNodesWithText("تعذر تشغيل البث", substring = true).fetchSemanticsNodes().isNotEmpty()
                    played || fallback
                } }.isSuccess
                row.put("playingCallback", played).put("fallbackVisible", fallback)
                if (played) {
                    val before = playerSnapshot()
                    SystemClock.sleep(3_000)
                    val after = playerSnapshot()
                    row.put("before", before ?: JSONObject.NULL).put("after", after ?: JSONObject.NULL)
                    row.put("progressConfirmed", after != null && before != null &&
                        before.optString("id").isNotBlank() && before.optString("id") == after.optString("id") &&
                        after.optDouble("time") > before.optDouble("time") + 1.0)
                }
                row.put("status", if (played || (observed && fallback)) "OBSERVED_PENDING_AUDIO_OR_SOURCE_RESTRICTION" else "NOT_VERIFIED")
                capture(output, "live_${source.id}")
            } catch (error: Exception) { row.put("status", "NOT_VERIFIED").put("reason", error.toString()) }
              catch (error: AssertionError) { row.put("status", "NOT_VERIFIED").put("reason", error.toString()) }
            finally {
                row.put("finishedAtEpochMs", System.currentTimeMillis())
                liveLedger.appendText(row.toString() + "\n")
            }
        }
    }
}
