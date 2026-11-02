package com.example.feature.azkar.data.local.database

import androidx.datastore.preferences.core.edit
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.azkar.data.local.dao.AzkarDao
import com.example.feature.azkar.data.local.entity.ZikrEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Phase 2B — Idempotent, versioned Azkar self-healing seed.
 *
 * Goals
 * - Repair an existing Room database whose `azkar_table` is empty or partial
 *   because the original `Room.Callback.onCreate` seed was skipped or interrupted.
 * - Never touch user state: `currentCount`, `isFavorite`, `dailyProgress`,
 *   `lastUpdatedDate` on existing rows are preserved.
 * - Run on app start AND on demand (AzkarViewModel observes the table and may
 *   trigger a repair when the screen first renders empty).
 * - Be idempotent: running twice in a row is a no-op.
 * - Be transactional per row: a crash mid-repair leaves the table in a
 *   superset-of-seed state, not a partial state.
 *
 * Mechanism
 * - A monotonic `CURRENT_SEED_VERSION` integer is compared against the
 *   `AZKAR_SEED_VERSION` DataStore key. If the stored version is lower (or
 *   missing or 0), the manager inserts only rows whose natural key
 *   (`title`, `category`, `text`) is absent, leaving any existing rows alone.
 * - A `Mutex` serializes concurrent calls. The `CoroutineScope` is owned by
 *   the manager; the public suspend API is safe to call from any context.
 */
class AzkarSeedManager(
    private val dao: AzkarDao,
    private val settings: AzkarSeedVersionStore,
    private val additionalRows: List<ZikrEntity> = emptyList()
) {

    /**
     * Convenience constructor used by Koin — accepts the production
     * [SettingsManager] and adapts it to the [AzkarSeedVersionStore] contract
     * through the [SettingsManagerBackedStore] adapter. Tests construct
     * [AzkarSeedManager] directly with an in-memory store.
     */
    constructor(
        dao: AzkarDao,
        settingsManager: SettingsManager,
        additionalRows: List<ZikrEntity> = emptyList()
    ) : this(dao, SettingsManagerBackedStore(settingsManager), additionalRows)

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * The seed version that the bundled library content ships with.
     * Bump this when the bundled seed rows change. The repair will be
     * triggered automatically the next time the app starts.
     */
    val currentSeedVersion: Int = CURRENT_SEED_VERSION

    /**
     * Idempotent self-healing entry point. Safe to call from any coroutine.
     * Returns the number of rows that were inserted in this call (0 if no
     * repair was needed or no repair was possible).
     */
    suspend fun ensureSeeded(): Int = mutex.withLock { ensureSeededLocked() }

    /**
     * Cheap, lazy safety-net variant. Runs the full [ensureSeeded] only when
     * the azkar_table is currently empty. Used by the AzkarViewModel on
     * screen open so process recreation that bypasses IhsanApp.onCreate
     * still heals an empty table without a synchronous DataStore read on
     * every screen visit.
     *
     * Returns true when a repair actually ran, false when the table was
     * already non-empty (no-op path).
     */
    suspend fun ensureSeededIfTableEmpty(): Boolean = mutex.withLock {
        if (dao.countZikr() > 0) return@withLock false
        // Inline the locked body to avoid a Mutex re-entrancy deadlock.
        ensureSeededLocked()
        return@withLock true
    }

    /**
     * Internal body. Must be called only while the [mutex] is held.
     */
    private suspend fun ensureSeededLocked(): Int {
        dao.repairCanonicalText("الصباح", "أذكار الصباح", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ", "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ", "صحيح مسلم 2723b — ذكر الصباح؛ ضبط مرجع على sunnah.com")
        dao.repairCanonicalText("آية الكرسي", "أذكار الصباح", "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...", "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ", "القرآن الكريم — البقرة 255؛ هدف قراءة واحدة، دون نسبة عدد أو فضل خاص بالصباح")
        dao.repairCanonicalText("آية الكرسي", "أذكار الصباح", "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...", "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ", "القرآن الكريم — البقرة 255؛ هدف قراءة واحدة، دون نسبة عدد أو فضل خاص بالصباح")
        dao.repairCanonicalText("المساء", "أذكار المساء", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ", "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ", "صحيح مسلم 2723b — ذكر المساء؛ ضبط مرجع على sunnah.com")
        dao.repairCanonicalText("بعد الصلاة", "أذكار بعد الصلاة", "أستغفر الله (3 مرات)", "أَسْتَغْفِرُ اللَّهَ", "صحيح مسلم 591 — باب استحباب الذكر بعد الصلاة")
        dao.repairCanonicalText("بعد الصلاة", "أذكار بعد الصلاة", "اللهم أنت السلام ومنك السلام...", "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ", "صحيح مسلم 591 — باب استحباب الذكر بعد الصلاة")
        val stored = settings.readSeedVersion()
        dao.repairCanonicalText("آية الكرسي", "أذكار الصباح", "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلۡحَيُّ ٱلۡقَيُّومُۚ لَا تَأۡخُذُهُۥ سِنَةٞ وَلَا نَوۡمٞۚ لَّهُۥ مَا فِي ٱلسَّمَٰوَٰتِ وَمَا فِي ٱلۡأَرۡضِۗ مَن ذَا ٱلَّذِي يَشۡفَعُ عِندَهُۥٓ إِلَّا بِإِذۡنِهِۦۚ يَعۡلَمُ مَا بَيۡنَ أَيۡدِيهِمۡ وَمَا خَلۡفَهُمۡۖ وَلَا يُحِيطُونَ بِشَيۡءٖ مِّنۡ عِلۡمِهِۦٓ إِلَّا بِمَا شَآءَۚ وَسِعَ كُرۡسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلۡأَرۡضَۖ وَلَا يَـُٔودُهُۥ حِفۡظُهُمَاۚ وَهُوَ ٱلۡعَلِيُّ ٱلۡعَظِيمُ", "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ", "القرآن الكريم — Tanzil 1.1، البقرة 255؛ هدف قراءة واحدة")
        if (stored >= CURRENT_SEED_VERSION) {
            // Even if the version matches, an old install might have a row set
            // that was deleted manually. Re-insert any missing canonical rows
            // without bumping the version. This is the partial-recovery path.
            val missing = missingSeedRows()
            if (missing.isEmpty()) return 0
            insertRows(missing)
            return missing.size
        }
        val missing = missingSeedRows()
        if (missing.isEmpty()) {
            // No missing rows; just record the version so we don't check again.
            settings.writeSeedVersion(CURRENT_SEED_VERSION)
            return 0
        }
        insertRows(missing)
        settings.writeSeedVersion(CURRENT_SEED_VERSION)
        return missing.size
    }

    /**
     * Fire-and-forget variant for app-start wiring. Returns immediately;
     * the actual repair runs in the manager's scope. Safe to call multiple
     * times — the mutex serializes.
     */
    fun ensureSeededAsync() {
        scope.launch { ensureSeeded() }
    }

    private suspend fun missingSeedRows(): List<ZikrEntity> = withContext(Dispatchers.IO) {
        (SEED_ROWS + additionalRows).filter { seed ->
            dao.countByContentKey(
                title = seed.title,
                category = seed.category,
                text = seed.text
            ) == 0
        }
    }

    private suspend fun insertRows(rows: List<ZikrEntity>) = withContext(Dispatchers.IO) {
        rows.forEach { dao.insertZikr(it) }
    }

    companion object {
        /**
         * Bump this constant when the canonical seed set changes. Older
         * installs will detect the mismatch and run the repair.
         */
        const val CURRENT_SEED_VERSION: Int = 5

        /**
         * Canonical seed rows. The natural key for repair detection is
         * (`title`, `category`, `text`). Changing any of these fields
         * requires bumping [CURRENT_SEED_VERSION] so existing rows are
         * re-detected as "missing the new definition" only if the user
         * deleted the old row manually.
         *
         * The list mirrors what `DatabaseModule.onCreate` originally wrote
         * for fresh installs, plus the seven canonical tasbih counters.
         */
        internal val SEED_ROWS: List<ZikrEntity> = listOf(
            ZikrEntity(title = "الصباح", text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذَا الْيَوْمِ وَخَيْرَ مَا بَعْدَهُ، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذَا الْيَوْمِ وَشَرِّ مَا بَعْدَهُ، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ", targetCount = 1, category = "أذكار الصباح", source = "صحيح مسلم 2723b — ذكر الصباح؛ ضبط مرجع على sunnah.com"),
            ZikrEntity(title = "آية الكرسي", text = "ٱللَّهُ لَآ إِلَٰهَ إِلَّا هُوَ ٱلْحَىُّ ٱلْقَيُّومُ لَا تَأْخُذُهُۥ سِنَةٌ وَلَا نَوْمٌ لَّهُۥ مَا فِى ٱلسَّمَٰوَٰتِ وَمَا فِى ٱلْأَرْضِ مَن ذَا ٱلَّذِى يَشْفَعُ عِندَهُۥٓ إِلَّا بِإِذْنِهِۦ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلَا يُحِيطُونَ بِشَىْءٍ مِّنْ عِلْمِهِۦٓ إِلَّا بِمَا شَآءَ وَسِعَ كُرْسِيُّهُ ٱلسَّمَٰوَٰتِ وَٱلْأَرْضَ وَلَا يَـُٔودُهُۥ حِفْظُهُمَا وَهُوَ ٱلْعَلِىُّ ٱلْعَظِيمُ", targetCount = 1, category = "أذكار الصباح", source = "القرآن الكريم — البقرة 255؛ هدف قراءة واحدة، دون نسبة عدد أو فضل خاص بالصباح"),
            ZikrEntity(title = "المساء", text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ وَالْحَمْدُ لِلَّهِ لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ، رَبِّ أَسْأَلُكَ خَيْرَ مَا فِي هَذِهِ اللَّيْلَةِ وَخَيْرَ مَا بَعْدَهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فِي هَذِهِ اللَّيْلَةِ وَشَرِّ مَا بَعْدَهَا، رَبِّ أَعُوذُ بِكَ مِنَ الْكَسَلِ وَسُوءِ الْكِبَرِ، رَبِّ أَعُوذُ بِكَ مِنْ عَذَابٍ فِي النَّارِ وَعَذَابٍ فِي الْقَبْرِ", targetCount = 1, category = "أذكار المساء", source = "صحيح مسلم 2723b — ذكر المساء؛ ضبط مرجع على sunnah.com"),
            ZikrEntity(title = "بعد الصلاة", text = "أَسْتَغْفِرُ اللَّهَ", targetCount = 3, category = "أذكار بعد الصلاة", source = "صحيح مسلم 591 — باب استحباب الذكر بعد الصلاة"),
            ZikrEntity(title = "بعد الصلاة", text = "اللَّهُمَّ أَنْتَ السَّلَامُ وَمِنْكَ السَّلَامُ تَبَارَكْتَ ذَا الْجَلَالِ وَالْإِكْرَامِ", targetCount = 1, category = "أذكار بعد الصلاة", source = "صحيح مسلم 591 — باب استحباب الذكر بعد الصلاة"),
            ZikrEntity(title = "تسبيح", text = "سُبْحَانَ اللَّهِ", targetCount = 33, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "تحميد", text = "الْحَمْدُ لِلَّهِ", targetCount = 33, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "تكبير", text = "اللَّهُ أَكْبَرُ", targetCount = 33, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "التهليل", text = "لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ، وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ", targetCount = 10, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "الحوقلة", text = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ", targetCount = 33, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "الاستغفار", text = "أَسْتَغْفِرُ اللَّهَ وَأَتُوبُ إِلَيْهِ", targetCount = 100, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "تسبيح وبحمد", text = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ ، سُبْحَانَ اللَّهِ الْعَظِيمِ", targetCount = 33, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة"),
            ZikrEntity(title = "سبحة حرة", text = "اضغط للبدء بالتسبيح الحر", targetCount = 0, category = "تسبيح", source = "هدف عدّاد اختياري؛ لا ينسب هذا العدد إلى سنة خاصة")
        )
    }
}

/**
 * Tiny persistence contract used by [AzkarSeedManager] so the manager
 * itself has no compile-time dependency on DataStore. Production wiring
 * uses [SettingsManagerBackedStore]; tests provide an in-memory
 * implementation.
 */
interface AzkarSeedVersionStore {
    suspend fun readSeedVersion(): Int
    suspend fun writeSeedVersion(value: Int)
}

/**
 * Production adapter that persists the seed version through the existing
 * [SettingsManager] DataStore. The DataStore key is the same one exposed
 * by [SettingsManager.AZKAR_SEED_VERSION] for direct, Koin-injected reads
 * by the test suite and any future surface.
 */
private class SettingsManagerBackedStore(
    private val settingsManager: SettingsManager
) : AzkarSeedVersionStore {

    override suspend fun readSeedVersion(): Int {
        val prefs = settingsManager.dataStoreInternal.data.first()
        return prefs[SettingsManager.AZKAR_SEED_VERSION] ?: 0
    }

    override suspend fun writeSeedVersion(value: Int) {
        settingsManager.dataStoreInternal.edit { prefs ->
            prefs[SettingsManager.AZKAR_SEED_VERSION] = value
        }
    }
}
