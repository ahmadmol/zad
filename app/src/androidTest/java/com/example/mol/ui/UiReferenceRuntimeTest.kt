package com.example.mol.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/** Actual UI/navigation/data, using Android accessibility without replacing Compose's clock. */
class UiReferenceRuntimeTest {
    private val instrumentation get() = InstrumentationRegistry.getInstrumentation()
    private val automation get() = instrumentation.uiAutomation
    private val output get() = File(instrumentation.targetContext.getExternalFilesDir(null), "reference-runtime").apply { mkdirs() }
    private fun nodes(): List<AccessibilityNodeInfo> {
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo) {
            result += node
            for (i in 0 until node.childCount) node.getChild(i)?.let(::visit)
        }
        automation.rootInActiveWindow?.let(::visit)
        return result
    }
    private fun find(label: String): AccessibilityNodeInfo? {
        val visible = nodes().filter { it.isVisibleToUser }
        val exact = visible.filter { it.text?.toString() == label || it.contentDescription?.toString() == label }
        return (if (exact.isNotEmpty()) exact else visible.filter {
            it.text?.contains(label) == true || it.contentDescription?.contains(label) == true
        }).lastOrNull()
    }
    private fun scroll(forward: Boolean): Boolean {
        val node = nodes().firstOrNull { it.isScrollable && it.isVisibleToUser } ?: return false
        val changed = node.performAction(if (forward) AccessibilityNodeInfo.ACTION_SCROLL_FORWARD else AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)
        SystemClock.sleep(160)
        return changed
    }
    private fun top() { repeat(7) { if (!scroll(false)) return } }
    private fun click(label: String, allowScroll: Boolean = false) {
        val deadline = SystemClock.uptimeMillis() + 15_000
        var node: AccessibilityNodeInfo? = null
        var attempts = 0
        while (node == null && SystemClock.uptimeMillis() < deadline) {
            node = find(label)
            while (node != null && !node.isClickable) node = node.parent
            if (node == null) {
                if (allowScroll && attempts++ < 7) scroll(true)
                SystemClock.sleep(200)
            }
        }
        val target = checkNotNull(node) { "Missing actionable UI label: $label" }
        check(target.performAction(AccessibilityNodeInfo.ACTION_CLICK)) { "Click rejected: $label" }
        SystemClock.sleep(450)
    }
    private fun tab(label: String) = click(label)
    private fun services() {
        tab("الرئيسية")
        repeat(7) { if(find("اكتشف الخدمات") == null) scroll(true) }
        checkNotNull(find("اكتشف الخدمات")) { "The full services entry is not reachable" }
        click("عرض الكل")
    }
    private fun back() {
        // Use the app's visible toolbar action; raw emulator key events can finish
        // the instrumentation-launched activity instead of invoking its toolbar route.
        if (find("رجوع") != null) { click("رجوع"); return }
        automation.injectInputEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK), true)
        automation.injectInputEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK), true)
        SystemClock.sleep(400)
    }
    private fun shell(command: String) {
        android.os.ParcelFileDescriptor.AutoCloseInputStream(automation.executeShellCommand(command)).use { it.readBytes() }
    }
    private fun shot(name: String) {
        SystemClock.sleep(400)
        check(automation.rootInActiveWindow?.packageName?.toString() == instrumentation.targetContext.packageName) {
            "Another application/dialog covers the QA screen: $name"
        }
        val bitmap = checkNotNull(automation.takeScreenshot())
        File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    @Test fun productionReferenceScreensAcrossPhoneSizesAndThemes() {
        assumeTrue(Build.HARDWARE == "ranchu" || Build.HARDWARE == "goldfish")
        val pkg = instrumentation.targetContext.packageName
        assumeTrue(pkg.endsWith(".uicorrection"))
        instrumentation.startActivitySync(Intent(Intent.ACTION_MAIN).setClassName(pkg, "com.example.mol.MainActivity")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        val startDeadline = SystemClock.uptimeMillis() + 20_000
        while(find("تخطي") == null && find("الملف الشخصي") == null && SystemClock.uptimeMillis() < startDeadline) SystemClock.sleep(250)
        if (find("تخطي") != null) click("تخطي")
        for ((width,height) in listOf(360 to 800,393 to 852,412 to 915,600 to 960)) {
            shell("wm size ${width}x$height")
            SystemClock.sleep(600)
            for (dark in listOf(false,true)) {
                tab("الملف الشخصي")
                if (dark || width != 360) click("الوضع الداكن", true)
                top()
                val prefix="${width}_${if(dark) "dark" else "light"}"
                shot("${prefix}_profile")
                click("جميع الإعدادات", true); shot("${prefix}_settings"); back()
                tab("الرئيسية"); top(); shot("${prefix}_home")
                tab("إحسان"); top(); click("عروض التبرع"); shot("${prefix}_offers")
                click("طلبات مساعدة"); shot("${prefix}_requests"); click("عروض التبرع")
                for ((label,file) in listOf("الصلاة" to "prayer","القبلة" to "qibla","الأذكار" to "azkar",
                    "الأدعية" to "dua","التسبيح" to "tasbih","الحديث" to "hadith","الأسماء الحسنى" to "asma",
                    "جدول الأعمال" to "daily","الإحصائيات" to "statistics","بحث" to "search",
                    "القرآن" to "quran_list","البث المباشر" to "live")) {
                    services(); click(label)
                    if (file=="live") click("الحرم")
                    shot("${prefix}_$file")
                    if(file=="quran_list") { click("الفاتحة",true); shot("${prefix}_reader"); back() }
                    if(file=="dua") { click("دعاء قضاء الحاجة"); shot("${prefix}_dua_detail"); back() }
                    back()
                }
                tab("الرئيسية"); top(); click("الإشعارات"); shot("${prefix}_reminders"); back()
            }
        }
    }
}
