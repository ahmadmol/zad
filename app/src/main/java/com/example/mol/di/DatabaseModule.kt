package com.example.mol.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.feature.azkar.data.local.entity.ZikrEntity
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.quran.data.local.QuranAssetLoader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<IhsanDatabase> {
        val context = androidContext()
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add explanation column to hadiths table; existing rows will have NULL
                database.execSQL("ALTER TABLE hadiths ADD COLUMN explanation TEXT")
            }
        }

        Room.databaseBuilder(
            context,
            IhsanDatabase::class.java,
            "ihsan_master_db"
        ).addMigrations(MIGRATION_2_3)
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                    // Azkar Population
                    val azkarDao = get<IhsanDatabase>().azkarDao()
                    if (azkarDao.countZikr() == 0) {
                        azkarDao.insertZikr(ZikrEntity(title = "الصباح", text = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ", targetCount = 1, category = "أذكار الصباح"))
                        azkarDao.insertZikr(ZikrEntity(title = "آية الكرسي", text = "اللَّهُ لَا إِلَهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ...", targetCount = 1, category = "أذكار الصباح"))
                        azkarDao.insertZikr(ZikrEntity(title = "المساء", text = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لَا إِلَهَ إِلَّا اللَّهُ وَحْدَهُ لَا شَرِيكَ لَهُ", targetCount = 1, category = "أذكار المساء"))
                        azkarDao.insertZikr(ZikrEntity(title = "بعد الصلاة", text = "أستغفر الله (3 مرات)", targetCount = 3, category = "أذكار بعد الصلاة"))
                        azkarDao.insertZikr(ZikrEntity(title = "بعد الصلاة", text = "اللهم أنت السلام ومنك السلام...", targetCount = 1, category = "أذكار بعد الصلاة"))
                        azkarDao.insertZikr(ZikrEntity(title = "تسبيح", text = "سُبْحَانَ اللَّهِ", targetCount = 33, category = "تسابيح عامة"))
                        azkarDao.insertZikr(ZikrEntity(title = "تحميد", text = "الْحَمْدُ لِلَّهِ", targetCount = 33, category = "تسابيح عامة"))
                        azkarDao.insertZikr(ZikrEntity(title = "تكبير", text = "اللَّهُ أَكْبَرُ", targetCount = 33, category = "تسابيح عامة"))
                        azkarDao.insertZikr(ZikrEntity(title = "سبحة", text = "سبحة حرة", targetCount = 0, category = "سبحة حرة"))
                    }

                    // Quran Population
                    get<QuranAssetLoader>().loadIfNeeded()
                }
            }
        }).build()
    }

    single { get<IhsanDatabase>().azkarDao() }
    single { get<IhsanDatabase>().duaDao() }
    single { get<IhsanDatabase>().donationDao() }
    single { get<IhsanDatabase>().userDao() }
    single { get<IhsanDatabase>().hadithDao() }
    single { get<IhsanDatabase>().quranDao() }
    single { get<IhsanDatabase>().downloadDao() }
}
