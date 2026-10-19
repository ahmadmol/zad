package com.example.mol.ui

import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import androidx.test.platform.app.InstrumentationRegistry
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.mol.navigation.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.Locale

/** Production navigation graph. Physical mode preserves the existing location and
 * uses the phone's native density; rendering is separate from functional acceptance. */
class DarkFinalRouteMatrixTest {
    @get:Rule(order = 0) val preservePreferences = PreserveDevicePreferences()
    @get:Rule(order = 1) val compose = createAndroidComposeRule<ComponentActivity>()
    private val routes = listOf(
        "home" to Screen.Home.route, "community" to Screen.Donations.route,
        "profile" to Screen.Profile.route, "prayer" to Screen.Prayer.route,
        "qibla" to Screen.Qibla.route, "quran_list" to Screen.Quran.route,
        "quran_reader" to Screen.QuranReader.createRoute(1),
        "azkar" to Screen.Azkar.route, "duas" to Screen.Dua.route,
        "dua_detail" to Screen.DuaDetail.createRoute(22201), "hadith" to Screen.Hadith.route,
        "asma" to Screen.Asma.route, "tasbih" to Screen.Tasbih.route,
        "settings" to Screen.Settings.route, "statistics" to Screen.Statistics.route,
        "reminders" to Screen.Reminders.route, "search" to Screen.GlobalSearch.route,
        "daily" to Screen.DailyActivities.route, "edit_profile" to Screen.EditProfile.route,
        "history" to Screen.DonationHistory.route, "manage_offers" to Screen.ManageListings.createRoute("OFFER"),
        "manage_requests" to Screen.ManageListings.createRoute("REQUEST"),
        "add_offer" to Screen.AddDonation.createRoute("OFFER"), "add_request" to Screen.AddDonation.createRoute("REQUEST"),
        "request_help" to Screen.RequestHelp.route, "offer_detail" to Screen.IhsanDetails.createRoute(1),
        "missing_offer" to Screen.IhsanDetails.createRoute(999999),
        "live_haram" to Screen.HaramLive.route, "live_nabawi" to Screen.NabawiLive.route,
        "sanhya" to Screen.SanhyaMain.route,
        "sanhya_detail" to Screen.SanhyaStoryDetail.createRoute("story_adam_sons"),
        "sanhya_episodes" to Screen.SanhyaEpisodes.createRoute("story_adam_sons"),
        "sanhya_verses" to Screen.SanhyaQuranVerses.createRoute("story_adam_sons"),
        "sanhya_favorites" to Screen.SanhyaFavorites.route, "sanhya_later" to Screen.SanhyaWatchLater.route,
        "sanhya_search" to Screen.SanhyaSearch.createRoute("آدم"), "sanhya_settings" to Screen.SanhyaSettings.route,
        "nabi" to Screen.NabiIhsanMain.route, "nabi_detail" to Screen.NabiIhsanEpisodeDetails.createRoute("ep_01"),
        "nabi_episodes" to Screen.NabiIhsanEpisodes.route, "nabi_recipes" to Screen.NabiIhsanRecipes.route,
        "nabi_recipe_detail" to Screen.NabiIhsanRecipeDetails.createRoute("missing"),
        "nabi_favorites" to Screen.NabiIhsanFavorites.route, "nabi_later" to Screen.NabiIhsanWatchLater.route,
        "nabi_search" to Screen.NabiIhsanSearch.createRoute("الإحسان"), "nabi_settings" to Screen.NabiIhsanSettings.route,
        "fahm" to Screen.FahmMain.route, "fahm_detail" to Screen.FahmEpisodeDetail.createRoute("ep_01"),
        "fahm_episodes" to Screen.FahmEpisodes.route, "fahm_stations" to Screen.FahmStations.route,
        "fahm_journey" to Screen.FahmJourney.route, "fahm_search" to Screen.FahmSearch.createRoute("الله"),
        "fahm_saved" to Screen.FahmSaved.createRoute(), "fahm_completion" to Screen.FahmCompletion.createRoute("ep_01"),
        "fahm_about" to Screen.FahmAbout.route, "fahm_settings" to Screen.FahmSettings.route,
        "controlled_daily" to Screen.IhsanPlusDaily.route,
        "location" to Screen.LocationPermission.route, "onboarding" to Screen.Onboarding.route
    )

    @Test fun allProductionDestinationsInBothThemes() {
        val inst = InstrumentationRegistry.getInstrumentation()
        val args = InstrumentationRegistry.getArguments()
        val width = args.getString("darkWidth")?.toInt() ?: 393
        val font = args.getString("darkFont")?.toFloat() ?: 1f
        val physical = args.getString("physicalDeviceSerial") == "ac190123"
        if (physical) {
            check(android.os.Build.MODEL == "23129RAA4G")
            check(inst.targetContext.packageName == "com.example.mol.referenceqa")
        }
        val output = File(inst.targetContext.getExternalFilesDir(null),
            if (physical) "physical-routes-${System.currentTimeMillis()}" else "video-audit-matrix-20261015/$width-$font").apply { mkdirs() }
        val dark = mutableStateOf(false)
        lateinit var nav: NavHostController
        compose.setContent {
            val original = LocalConfiguration.current
            val pixels = LocalContext.current.resources.displayMetrics.widthPixels
            val config = Configuration(original).apply {
                screenWidthDp = width; fontScale = font; setLocale(Locale.forLanguageTag("ar"))
            }
            CompositionLocalProvider(LocalConfiguration provides config,
                LocalDensity provides if (physical) LocalDensity.current else Density(pixels.toFloat()/width, font), LocalLayoutDirection provides LayoutDirection.Rtl) {
                IhsanTheme(darkTheme = dark.value) {
                    val controller = rememberNavController()
                    nav = controller
                    val entry by controller.currentBackStackEntryAsState()
                    val selection = resolveBottomBarDestination(entry?.destination?.route)
                    val items = listOf(Screen.Home, Screen.Donations, Screen.Profile)
                    val selected = items.indexOfFirst { it.route == selection?.route }
                    Scaffold(contentWindowInsets = WindowInsets(0,0,0,0),
                        bottomBar = {
                            if(selected >= 0) Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal=8.dp,vertical=4.dp)) {
                                IhsanBottomNavigation(listOf(
                                    IhsanBottomNavDestination("الرئيسية",Icons.Default.Home,Icons.Default.Home),
                                    IhsanBottomNavDestination("إحسان",Icons.Default.Favorite,Icons.Default.Favorite),
                                    IhsanBottomNavDestination("الملف الشخصي",Icons.Default.Person,Icons.Default.Person)),
                                    selected,{ controller.navigate(items[it].route) },Modifier.fillMaxWidth())
                            }
                        }) { padding ->
                        AppNavHost(controller,Modifier.padding(bottom=padding.calculateBottomPadding()))
                    }
                }
            }
        }
        compose.waitForIdle(); SystemClock.sleep(3500)
        val failures=mutableListOf<String>()
        val geometry=mutableListOf<String>()
        var cases=0
        val requested = args.getString("darkRoutes")?.split(",")?.toSet()
        routes.filter { requested == null || it.first in requested }.forEach { (name,route) ->
            compose.runOnIdle { nav.navigate(route) { popUpTo(nav.graph.id) { inclusive=false } } }
            compose.waitForIdle(); SystemClock.sleep(if (name in listOf("sanhya_detail", "fahm_detail")) 2500 else 700)
            if (name == "quran_reader") compose.waitUntil(30_000) {
                compose.onAllNodesWithText("سُورَةُ الفاتحة").fetchSemanticsNodes().isNotEmpty()
            }
            if (name == "hadith") {
                // The full publisher corpus is parsed/seeded asynchronously; compare settled content.
                compose.waitUntil(30_000) { compose.onAllNodesWithText("HadeethEnc.com",substring=true).fetchSemanticsNodes().isNotEmpty() }
                compose.waitForIdle()
            }
            var lightBounds: Map<String,String>? = null
            for(night in listOf(false,true)) {
                compose.runOnIdle { dark.value=night }
                compose.waitForIdle(); SystemClock.sleep(450)
                val prefix="${if(night) "dark" else "light"}_$name"
                val rootNodes=compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsProperties.Text),useUnmergedTree=true).fetchSemanticsNodes()
                assertTrue("Empty production destination: $prefix",rootNodes.isNotEmpty())
                if(name.startsWith("fahm_") && rootNodes.isNotEmpty()) {
                    val statusHeight = androidx.core.view.ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                        ?.getInsets(androidx.core.view.WindowInsetsCompat.Type.statusBars())?.top ?: 0
                    val headerLabels = setOf("الدرس 01", "إتمام الدرس", "رحلتي في الفهم عن الله",
                        "منازل الرحلة", "إعدادات الفهم عن الله", "المحفوظات", "الفهم عن الله",
                        "ابحث في الفهم عن الله", "عن الفهم عن الله")
                    val header = rootNodes.firstOrNull { node ->
                        node.config[SemanticsProperties.Text].any { it.text in headerLabels }
                    }
                    if (header != null) assertTrue("Header overlaps status bar: $prefix",
                        header.boundsInWindow.top >= statusHeight - 1f)
                }
                val occurrences=mutableMapOf<String,Int>()
                val bounds=rootNodes.associate { node ->
                    val label=node.config[SemanticsProperties.Text].joinToString()
                    val occurrence=occurrences.getOrDefault(label,0)
                    occurrences[label]=occurrence+1
                    "$label#$occurrence" to node.boundsInRoot.toString()
                }
                if(!night) lightBounds=bounds else {
                    // Ignore time/date changing text, but stable labels must keep their bounds.
                    bounds.forEach { (label,rect) ->
                        lightBounds?.get(label)?.let { previous -> if(previous!=rect) geometry += "$name: $label: $previous -> $rect" }
                    }
                }
                val layouts=compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),useUnmergedTree=true)
                layouts.fetchSemanticsNodes().indices.forEach { index ->
                    val results=mutableListOf<TextLayoutResult>()
                    layouts[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                    results.forEach { result ->
                        val ellipsized=(0 until result.lineCount).any { result.isLineEllipsized(it) }
                        if(result.didOverflowHeight && !ellipsized) failures += "$prefix: ${result.layoutInput.text}"
                    }
                }
                // Native emulator framebuffer including system bars, not reference-image composites.
                assertTrue("Another window obscures production destination: $prefix", compose.activity.hasWindowFocus())
                val bitmap=checkNotNull(inst.uiAutomation.takeScreenshot())
                File(output,"$prefix.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG,100,it) }
                bitmap.recycle()
                // Scroll every scrollable destination and preserve a second native screenshot.
                val scrollers=compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.ScrollBy) and
                    SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange),useUnmergedTree=true)
                if(scrollers.fetchSemanticsNodes().isNotEmpty()) {
                    // Prefer the vertical body over horizontal category chips. A single short
                    // swipe does not establish that the final controls/content are reachable.
                    var reachedEnd = false
                    for (step in 0 until 12) {
                        val range = scrollers[0].fetchSemanticsNode().config[SemanticsProperties.VerticalScrollAxisRange]
                        if (range.value() >= range.maxValue() - .5f) { reachedEnd = true; break }
                        scrollers[0].performSemanticsAction(SemanticsActions.ScrollBy) { it(0f,1_000_000f) }
                        compose.waitForIdle()
                    }
                    assertTrue("Could not reach the end of vertical content: $prefix", reachedEnd)
                    compose.waitForIdle()
                    assertTrue("Another window obscures bottom capture: $prefix", compose.activity.hasWindowFocus())
                    SystemClock.sleep(450)
                    val lower=checkNotNull(inst.uiAutomation.takeScreenshot())
                    File(output,"${prefix}_lower.png").outputStream().use { lower.compress(Bitmap.CompressFormat.PNG,100,it) };lower.recycle()
                    val bottomLayouts=compose.onAllNodes(SemanticsMatcher.keyIsDefined(SemanticsActions.GetTextLayoutResult),useUnmergedTree=true)
                    bottomLayouts.fetchSemanticsNodes().indices.forEach { index ->
                        val results=mutableListOf<TextLayoutResult>()
                        bottomLayouts[index].performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
                        results.forEach { result ->
                            val ellipsized=(0 until result.lineCount).any { result.isLineEllipsized(it) }
                            if(result.didOverflowHeight && !ellipsized) failures += "${prefix}_bottom: ${result.layoutInput.text}"
                        }
                    }
                    if (scrollers[0].fetchSemanticsNode().config.contains(SemanticsActions.ScrollToIndex)) scrollers[0].performScrollToIndex(0)
                    else scrollers[0].performSemanticsAction(SemanticsActions.ScrollBy) { it(0f,-1_000_000f) }
                    compose.waitForIdle()
                }
                cases++
                File(output,"route-results.jsonl").appendText(org.json.JSONObject()
                    .put("case",name).put("route",route).put("theme",if(night) "dark" else "light")
                    .put("rendered",true).put("screenshot","$prefix.png")
                    .put("textOverflow",failures.filter { it.startsWith(prefix) })
                    .put("scope","DEBUG_PRODUCTION_GRAPH_RENDER_AND_SCROLL_NOT_FULL_FUNCTIONAL_PASS")
                    .toString()+"\n")
                inst.sendStatus(0,android.os.Bundle().apply { putString("physicalRouteOutput",output.absolutePath); putString("routeCase",prefix) })
            }
        }
        File(output,"geometry.txt").writeText(geometry.joinToString("\n"))
        File(output,"overflow.txt").writeText(failures.joinToString("\n"))
        File(output,"case-count.txt").writeText(cases.toString())
        assertTrue(failures.joinToString("\n"),failures.isEmpty())
        assertTrue(geometry.joinToString("\n"),geometry.isEmpty())
    }
}
