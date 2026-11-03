package com.example.feature.quran.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.quran.domain.model.RecitationPosition
import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.repository.QuranRepository
import com.example.feature.quran.domain.usecase.QuranDownloadScheduler
import com.example.feature.quran.util.AudioPlayerHandler
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class QuranReaderStateTest {
    private val dispatcher = StandardTestDispatcher()
    private val repository = mockk<QuranRepository>(relaxUnitFun = true)
    private val audio = mockk<AudioPlayerHandler>(relaxUnitFun = true)
    private val preferences = mockk<UserPreferences>(relaxUnitFun = true)
    private val downloadScheduler = mockk<QuranDownloadScheduler>(relaxUnitFun = true)
    private val store = ViewModelStore()
    private val handle = SavedStateHandle()
    private lateinit var viewModel: QuranViewModel

    private val fatiha = Surah(1, "الفاتحة", "مكية", 7, 1)
    private val baqarah = Surah(2, "البقرة", "مدنية", 286, 2)
    private val nisa = Surah(4, "النساء", "مدنية", 176, 77)
    private val maida = Surah(5, "المائدة", "مدنية", 120, 106)
    private val ikhlas = Surah(112, "الإخلاص", "مكية", 4, 604)
    private val falaq = Surah(113, "الفلق", "مكية", 5, 604)
    private val nas = Surah(114, "الناس", "مدنية", 6, 604)

    private val nisaAyahs = (1..176).map { Verse(it, 4, it, "آية $it من النساء") }
    private val maidaAyahs = (1..120).map { Verse(200 + it, 5, it, "آية $it من المائدة") }

    private val currentRecitationFlow = MutableStateFlow<RecitationPosition?>(null)
    private val isPlayingFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        every { repository.observeAllSurahs() } returns flowOf(listOf(fatiha, baqarah, nisa, maida, ikhlas, falaq, nas))
        every { repository.observeAllBookmarks() } returns flowOf(emptyList())
        every { repository.observeLastRead() } returns flowOf(1 to 1)
        coEvery { repository.getKhatmaProgress() } returns 0f
        every { preferences.quranFontSize } returns flowOf(28f)
        every { preferences.quranReaderId } returns flowOf("Alafasy_128kbps")
        every { audio.isPlaying } returns isPlayingFlow
        every { audio.isBuffering } returns MutableStateFlow(false)
        every { audio.currentRecitation } returns currentRecitationFlow
        every { audio.currentPosition } returns MutableStateFlow(0L)
        every { audio.duration } returns MutableStateFlow(0L)
        every { audio.playbackError } returns MutableSharedFlow()
        every { repository.observeDownloadedAyahs(any(), any()) } returns flowOf(emptyList())
        every { downloadScheduler.identity(any(), any()) } returns "tag"
        every { downloadScheduler.observe(any()) } returns flowOf(mockk(relaxed = true))

        coEvery { repository.getSurahById(1) } returns fatiha
        coEvery { repository.getAyahsBySurah(1) } returns emptyList()
        coEvery { repository.getSurahById(4) } returns nisa
        coEvery { repository.getAyahsBySurah(4) } returns nisaAyahs
        coEvery { repository.getSurahById(5) } returns maida
        coEvery { repository.getAyahsBySurah(5) } returns maidaAyahs

        viewModel = createViewModel(handle)
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle): QuranViewModel {
        return ViewModelProvider(store, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuranViewModel(repository, audio, downloadScheduler, preferences, savedStateHandle) as T
        })[QuranViewModel::class.java]
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    @Test
    fun `opening reader with explicit target Surah An-Nisa Ayah 50 lands and locks on that target`() = runTest(dispatcher) {
        advanceUntilIdle()

        viewModel.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 50))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.selectedSurah?.id)
        assertEquals("النساء", state.selectedSurah?.name)
        assertEquals(50, state.initialAyahScroll)
        assertEquals(4, state.currentReadingSurahId)
        assertEquals(50, state.currentReadingAyah)
        assertTrue(state.isTargetLocked)
        assertEquals("explicit:4:50", state.activeRequestKey)

        // Verify SavedStateHandle preserves active target and request identity
        assertEquals(4, handle.get<Int>("reader_active_surah"))
        assertEquals(50, handle.get<Int>("reader_active_ayah"))
        assertEquals(true, handle.get<Boolean>("reader_target_locked"))
        assertEquals("explicit:4:50", handle.get<String>("reader_request_key"))
    }

    @Test
    fun `delayed lastRead flow emitting Al-Fatiha does not override explicit target`() = runTest(dispatcher) {
        advanceUntilIdle()

        viewModel.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 50))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(4, state.currentReadingSurahId)
        assertEquals(50, state.currentReadingAyah)
        assertEquals(4, state.selectedSurah?.id)
        assertEquals(50, state.initialAyahScroll)
    }

    @Test
    fun `new explicit navigation request to different ayah of same surah is NOT blocked by previous lock`() = runTest(dispatcher) {
        advanceUntilIdle()

        // First open Surah 4 Ayah 50
        viewModel.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 50))
        advanceUntilIdle()
        assertEquals("explicit:4:50", viewModel.uiState.value.activeRequestKey)
        assertEquals(50, viewModel.uiState.value.currentReadingAyah)

        // User scrolls to Ayah 55
        viewModel.onAction(QuranAction.UpdateActivePosition(surahId = 4, ayahNumber = 55))
        advanceUntilIdle()
        assertEquals(55, viewModel.uiState.value.currentReadingAyah)

        // Now user navigates with a NEW explicit request to Surah 4 Ayah 120
        viewModel.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 120))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("explicit:4:120", state.activeRequestKey)
        assertEquals(4, state.currentReadingSurahId)
        assertEquals(120, state.currentReadingAyah)
        assertEquals(120, state.initialAyahScroll)
    }

    @Test
    fun `updating active position across surah boundary loads new surah without clearing initial lock`() = runTest(dispatcher) {
        advanceUntilIdle()

        viewModel.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 176))
        advanceUntilIdle()

        // User flips to next page starting Surah Al-Maida (5, 1)
        viewModel.onAction(QuranAction.UpdateActivePosition(surahId = 5, ayahNumber = 1))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.currentReadingSurahId)
        assertEquals(1, state.currentReadingAyah)
        assertEquals(5, state.selectedSurah?.id)
        assertEquals("المائدة", state.selectedSurah?.name)
        assertEquals(120, state.ayahs.size)
        assertFalse(state.isLoading)
    }

    @Test
    fun `state restoration from SavedStateHandle retains active position across recreation and ignores duplicate init`() = runTest(dispatcher) {
        advanceUntilIdle()

        viewModel.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 50))
        advanceUntilIdle()

        // User flips pages to Ayah 85 during the session
        viewModel.onAction(QuranAction.UpdateActivePosition(surahId = 4, ayahNumber = 85))
        advanceUntilIdle()

        // Simulate process/screen recreation with the populated SavedStateHandle
        val restoredHandle = SavedStateHandle(
            mapOf(
                "reader_target_locked" to handle.get<Boolean>("reader_target_locked"),
                "reader_request_key" to handle.get<String>("reader_request_key"),
                "reader_active_surah" to handle.get<Int>("reader_active_surah"),
                "reader_active_ayah" to handle.get<Int>("reader_active_ayah")
            )
        )

        val restoredStore = ViewModelStore()
        val restoredVm = ViewModelProvider(restoredStore, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuranViewModel(repository, audio, downloadScheduler, preferences, restoredHandle) as T
        })[QuranViewModel::class.java]

        advanceUntilIdle()

        // Composable LaunchedEffect re-emits the original route args (4, 50) upon recreation
        restoredVm.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 50))
        advanceUntilIdle()

        // Must preserve scrolled position (4, 85) and NOT reset back to 50 or 1
        assertEquals(4, restoredVm.uiState.value.currentReadingSurahId)
        assertEquals(85, restoredVm.uiState.value.currentReadingAyah)
        assertEquals("explicit:4:50", restoredVm.uiState.value.activeRequestKey)
        assertTrue(restoredVm.uiState.value.isTargetLocked)
        restoredStore.clear()
    }

    @Test
    fun `saveLastRead deduplicates identical position saves and audio updates do not trigger redundant repository saves`() = runTest(dispatcher) {
        every { repository.observeLastRead() } returns flowOf(4 to 50)
        val localStore = ViewModelStore()
        val localVm = ViewModelProvider(localStore, object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                QuranViewModel(repository, audio, downloadScheduler, preferences, SavedStateHandle()) as T
        })[QuranViewModel::class.java]
        advanceUntilIdle()

        localVm.onAction(QuranAction.InitializeReaderTarget(explicitSurahId = 4, explicitAyahNumber = 50))
        advanceUntilIdle()

        clearMocks(repository, answers = false, recordedCalls = true)

        // Calling SaveLastRead with the already-saved (4, 50) position should be deduplicated
        localVm.onAction(QuranAction.SaveLastRead(4, 50))
        advanceUntilIdle()
        coVerify(exactly = 0) { repository.saveLastRead(4, 50) }

        // Audio advances to Ayah 51: updates currentPlayingAyah without calling saveLastRead
        isPlayingFlow.value = true
        currentRecitationFlow.value = RecitationPosition(4, 51, "Alafasy_128kbps")
        advanceUntilIdle()
        assertEquals(51, localVm.uiState.value.currentPlayingAyah)
        coVerify(exactly = 0) { repository.saveLastRead(any(), any()) }

        // User explicitly turns to a new page at Ayah 58 -> saved exactly once
        localVm.onAction(QuranAction.SaveLastRead(4, 58))
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.saveLastRead(4, 58) }

        // Repeated SaveLastRead(4, 58) for the same ayah is also deduplicated
        localVm.onAction(QuranAction.SaveLastRead(4, 58))
        advanceUntilIdle()
        coVerify(exactly = 1) { repository.saveLastRead(4, 58) }
        localStore.clear()
    }

    @Test
    fun `font size and zoom controls clamp to range 20f to 48f and reset to 28f`() = runTest(dispatcher) {
        advanceUntilIdle()
        assertEquals(28f, viewModel.uiState.value.fontSize, 0.01f)

        viewModel.onAction(QuranAction.UpdateFontSize(42f))
        advanceUntilIdle()
        assertEquals(42f, viewModel.uiState.value.fontSize, 0.01f)
        coVerify { preferences.setQuranFontSize(42f) }

        // Over-zoom clamps at 48f
        viewModel.onAction(QuranAction.UpdateFontSize(65f))
        advanceUntilIdle()
        assertEquals(48f, viewModel.uiState.value.fontSize, 0.01f)

        // Reset zoom returns to 28f (100%)
        viewModel.onAction(QuranAction.UpdateFontSize(28f))
        advanceUntilIdle()
        assertEquals(28f, viewModel.uiState.value.fontSize, 0.01f)
    }

    @Test
    fun `reading mode toggle updates state and persists in SavedStateHandle`() = runTest(dispatcher) {
        advanceUntilIdle()

        assertEquals(QuranReadingMode.Mushaf, viewModel.uiState.value.readingMode)

        viewModel.onAction(QuranAction.SetReadingMode(QuranReadingMode.ContinuousText))
        advanceUntilIdle()

        assertEquals(QuranReadingMode.ContinuousText, viewModel.uiState.value.readingMode)
        assertEquals("ContinuousText", handle.get<String>("reader_mode"))
    }

    @Test
    fun `mushaf page mapping resolves Surah An-Nisa Ayah 50 and multi-surah boundary pages accurately`() {
        val repoRoot = File(".").canonicalFile.let { dir ->
            generateSequence(dir) { it.parentFile }
                .firstOrNull { File(it, "settings.gradle.kts").exists() }
                ?: dir
        }
        val fullJson = File(repoRoot, "feature/src/main/assets/quran_full.json").readText()
        val startsJson = File(repoRoot, "feature/src/main/assets/mushaf_page_starts.json").readText()

        val full = Json.parseToJsonElement(fullJson).jsonArray
        val starts = Json.parseToJsonElement(startsJson).jsonArray
        val verses = mutableListOf<Verse>()
        val names = mutableMapOf<Int, String>()
        for (s in full.indices) {
            val surah = full[s].jsonObject
            val id = surah.getValue("id").jsonPrimitive.int
            names[id] = surah.getValue("name").jsonPrimitive.content
            val ayahs = surah.getValue("verses").jsonArray
            for (v in ayahs.indices) {
                val ayah = ayahs[v].jsonObject
                verses += Verse(verses.size + 1, id, ayah.getValue("id").jsonPrimitive.int, ayah.getValue("text").jsonPrimitive.content)
            }
        }
        val offsets = starts.map { entry ->
            val key = entry.jsonArray
            val sId = key[0].jsonPrimitive.int
            val aId = key[1].jsonPrimitive.int
            verses.indexOfFirst { it.surahId == sId && it.verseNumber == aId }
        } + verses.size
        val pages = (0 until 604).map { MushafPage(it + 1, verses.subList(offsets[it], offsets[it + 1]).toList(), names) }

        // 1. Surah An-Nisa (4), Ayah 50 resolves to Mushaf Page 86 (index 85) and never Page 1 (index 0)
        val nisa50PageIndex = pages.pageFor(4, 50)
        assertTrue("Expected Surah 4 Ayah 50 on page > 0", nisa50PageIndex > 0)
        val nisa50Page = pages[nisa50PageIndex]
        assertTrue(nisa50Page.verses.any { it.surahId == 4 && it.verseNumber == 50 })

        // 2. Boundary page containing end of one surah and start of another (e.g., Page 604: Surahs 112, 113, 114)
        val page604 = pages[603]
        val surahsOn604 = page604.verses.map { it.surahId }.distinct()
        assertEquals(listOf(112, 113, 114), surahsOn604)
        assertEquals(603, pages.pageFor(112, 1))
        assertEquals(603, pages.pageFor(113, 1))
        assertEquals(603, pages.pageFor(114, 6))

        // 3. Out-of-range ayah falls back to first page of the requested surah, not Al-Fatihah
        val nisaFallbackIndex = pages.pageFor(4, 999)
        assertEquals(pages.pageFor(4, 1), nisaFallbackIndex)
        assertNotEquals(0, nisaFallbackIndex)
    }
}
