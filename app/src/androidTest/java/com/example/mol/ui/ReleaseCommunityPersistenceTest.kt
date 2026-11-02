package com.example.mol.ui

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.core.data.local.database.IhsanDatabaseMigrations
import com.example.feature.ehsan.data.demo.CommunityDemoManager
import com.example.feature.ehsan.data.local.entity.UserEntity
import com.example.feature.ehsan.data.repository.EhsanRepositoryImpl
import com.example.feature.ehsan.data.repository.UserRepositoryImpl
import com.example.feature.ehsan.domain.model.Donation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ReleaseCommunityPersistenceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun imageEditReplacesPrivateFilesAtomicallyAndFailedCopyRetainsPriorListing() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, IhsanDatabase::class.java).build()
        val store = com.example.feature.ehsan.data.image.EhsanImageStore(context)
        val source = java.io.File(context.cacheDir, "audit-image-${System.nanoTime()}.png")
        val bitmap = android.graphics.Bitmap.createBitmap(8, 8, android.graphics.Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.GREEN)
        source.outputStream().use { bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
        var first: String? = null
        var replacement: String? = null
        try {
            db.userDao().insertUser(UserEntity(id = 1, firstName = "اختبار", lastName = "صور", phoneNumber = "qa-images"))
            val repo = EhsanRepositoryImpl(db.donationDao(), users = db.userDao(), imageStore = store)
            first = store.persist(android.net.Uri.fromFile(source))!!
            repo.addDonation(Donation(title = "صورة أصلية", description = "سجل اختبار صور محلي", category = "تعليم",
                location = "حلب", type = "OFFER", donorName = "اختبار", phoneNumber = "", imageUrl = first))
            val row = repo.getMyDonations("local-profile:1").first().single()
            repo.updateDonation(row.copy(title = "صورة معدلة"), listOf(android.net.Uri.fromFile(source).toString()))
            val edited = repo.getDonationById(row.id)!!
            replacement = edited.imageUrl!!
            assertEquals("صورة معدلة", edited.title)
            assertNotEquals(first, replacement)
            assertNull(store.resolve(first)); assertNotNull(store.resolve(replacement))
            val missing = android.net.Uri.fromFile(java.io.File(context.cacheDir, "missing-audit-image.png")).toString()
            assertTrue(runCatching { repo.updateDonation(edited.copy(title = "لا يحفظ"), listOf(missing)) }.isFailure)
            assertEquals("صورة معدلة", repo.getDonationById(row.id)!!.title)
            assertEquals(replacement, repo.getDonationById(row.id)!!.imageUrl)
            repo.updateDonation(edited, emptyList())
            assertNull(repo.getDonationById(row.id)!!.imageUrl)
            assertNull(store.resolve(replacement))
            assertEquals("local-profile:1", repo.getDonationById(row.id)!!.ownerId)
        } finally { first?.let { store.delete(it) }; replacement?.let { store.delete(it) }; source.delete(); db.close() }
    }

    @Test fun ownedOfferRequestCrudDemoResetAndProfileReopen() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, IhsanDatabase::class.java).build()
        try {
            val users = db.userDao()
            users.insertUser(UserEntity(id=1, firstName="ملف اختبار", lastName="أ", phoneNumber="local-a"))
            users.insertUser(UserEntity(id=2, firstName="ملف اختبار", lastName="أ", phoneNumber="local-b", isActive=false))
            val sessions = UserRepositoryImpl(users)
            val demo = CommunityDemoManager(db)
            val repo = EhsanRepositoryImpl(db.donationDao(), users=users, demo=demo)
            assertEquals(12, repo.getAllDonations().first().count { it.origin=="DEMO" })
            repeat(2) { index -> repo.addDonation(Donation(title="سجل اختبار $index", description="نص اختبار محلي", category="تعليم", location="حلب",
                type=if(index==0) "OFFER" else "REQUEST", donorName="اسم مكرر", phoneNumber="", ownerId="forged-owner", origin="DEMO")) }
            val mine = repo.getMyDonations("local-profile:1").first()
            assertEquals(2, mine.size)
            assertTrue(mine.all { it.ownerId=="local-profile:1" && it.origin=="USER" })
            val offer = mine.first { it.type=="OFFER" }
            repo.updateDonation(offer.copy(title="عنوان معدل", description="وصف معدل", status="PENDING", ownerId="forged-again", imageUrl="fake-image"))
            val edited = repo.getDonationById(offer.id)!!
            assertEquals("عنوان معدل", edited.title); assertEquals("PENDING", edited.status)
            assertEquals(offer.ownerId, edited.ownerId); assertEquals(offer.imageUrl, edited.imageUrl)
            repo.updateDonationStatus(offer.id,"COMPLETED")
            assertEquals("COMPLETED",repo.getDonationById(offer.id)!!.status)
            sessions.logout()
            assertNull(users.getUser().first())
            assertTrue(repo.getMyDonations("local-profile:1").first().isEmpty())
            assertEquals(1L,sessions.login("local-a")!!.id)
            assertEquals(2,repo.getMyDonations("local-profile:1").first().size)
            sessions.login("local-b")
            val attempt = runCatching { repo.deleteDonation(offer) }
            assertTrue("Same display name must not authorize another profile",attempt.exceptionOrNull() is SecurityException)
            assertNotNull(repo.getDonationById(offer.id))
            sessions.login("local-a")
            demo.resetDemoOnly(); demo.ensureSeeded(); demo.resetDemoOnly()
            val after = repo.getAllDonations().first()
            assertEquals(12,after.count { it.origin=="DEMO" })
            assertEquals(2,after.count { it.origin=="USER" })
            assertTrue(after.filter { it.origin=="DEMO" }.all { it.phoneNumber.isBlank() && it.title.contains("DEMO") })
            assertEquals(1L,users.getUser().first()!!.id)
            repo.getMyDonations("local-profile:1").first().forEach { repo.deleteDonation(it) }
            assertTrue(repo.getMyDonations("local-profile:1").first().isEmpty())
            assertEquals(12,repo.getAllDonations().first().size)
        } finally { db.close() }
    }

    @Test fun upgradeSixToEightPreservesLegacyRowsCountersImagesAndPrayerLog() {
        val name = "release-migration-"+UUID.randomUUID()+".db"
        val source = InstrumentationRegistry.getInstrumentation().context.assets.open("migrations/6.json").bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        val file = context.getDatabasePath(name); file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(file,null).use { sqlite ->
            val entities = source.getJSONArray("entities")
            for(i in 0 until entities.length()) {
                val entity=entities.getJSONObject(i); val table=entity.getString("tableName")
                sqlite.execSQL(entity.getString("createSql").replace((36.toChar().toString()+"{TABLE_NAME}"),table))
                val indices=entity.getJSONArray("indices")
                for(j in 0 until indices.length()) sqlite.execSQL(indices.getJSONObject(j).getString("createSql").replace((36.toChar().toString()+"{TABLE_NAME}"),table))
            }
            sqlite.execSQL("CREATE TABLE room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            sqlite.execSQL("INSERT INTO room_master_table VALUES(42,?)",arrayOf(source.getString("identityHash")))
            sqlite.execSQL("INSERT INTO users VALUES(42,'أ','ب','local-a','حلب','عنوان','USER')")
            sqlite.execSQL("INSERT INTO users VALUES(43,'أ','ب','local-b','دمشق','عنوان آخر','USER')")
            sqlite.execSQL("INSERT INTO donations VALUES(11,'تت','عع','كتب','حلب','OFFER','AVAILABLE','أ ب','private-local','content://old/provider/album',1234)")
            sqlite.execSQL("INSERT INTO donations VALUES(12,'حقائب مدرسية لأطفال حلب','وصف المثال القديم','أخرى','حلب','OFFER','AVAILABLE','جمعية الإحسان','+963912345678','drawable/ihsan_ehsan_school_backpack',1235)")
            sqlite.execSQL("INSERT INTO azkar_table (id,title,text,currentCount,targetCount,category,isFavorite,source,dailyProgress,lastUpdatedDate) VALUES(21,'ذكر','نص موجود',9,33,'تسبيح',1,'مرجع موجود',4,'2026-10-13')")
            sqlite.execSQL("CREATE TABLE prayer_log (date TEXT, prayer TEXT, completed INTEGER)")
            sqlite.execSQL("INSERT INTO prayer_log VALUES('2026-10-13','FAJR',1)")
            sqlite.version=6
        }
        val db=Room.databaseBuilder(context,IhsanDatabase::class.java,name).addMigrations(*IhsanDatabaseMigrations.ALL).build()
        try {
            val sqlite=db.openHelper.readableDatabase
            assertEquals(8,sqlite.version)
            sqlite.query("SELECT title,description,imageUrl,origin,ownerId FROM donations WHERE id=11").use { c ->
                assertTrue(c.moveToFirst());assertEquals("تت",c.getString(0));assertEquals("عع",c.getString(1));assertEquals("content://old/provider/album",c.getString(2));assertEquals("LEGACY",c.getString(3));assertTrue(c.isNull(4))
            }
            sqlite.query("SELECT phoneNumber,origin,title FROM donations WHERE id=12").use { c -> assertTrue(c.moveToFirst());assertEquals("",c.getString(0));assertEquals("DEMO",c.getString(1));assertTrue(c.getString(2).startsWith("[DEMO]")) }
            sqlite.query("SELECT currentCount,isFavorite,dailyProgress FROM azkar_table WHERE id=21").use { c -> assertTrue(c.moveToFirst());assertEquals(9,c.getInt(0));assertEquals(1,c.getInt(1));assertEquals(4,c.getInt(2)) }
            sqlite.query("SELECT COUNT(*) FROM prayer_log WHERE completed=1").use { c -> assertTrue(c.moveToFirst());assertEquals(1,c.getInt(0)) }
            sqlite.query("SELECT COUNT(*) FROM users").use { c -> assertTrue(c.moveToFirst());assertEquals(2,c.getInt(0)) }
        } finally { db.close();context.deleteDatabase(name) }
    }
}
