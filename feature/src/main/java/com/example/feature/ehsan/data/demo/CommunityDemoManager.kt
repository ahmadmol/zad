package com.example.feature.ehsan.data.demo

import androidx.room.withTransaction
import com.example.feature.core.data.local.database.IhsanDatabase
import com.example.feature.ehsan.data.local.entity.DonationEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Deterministic examples only; reset never touches user rows, profiles or personal settings. */
class CommunityDemoManager(private val database: IhsanDatabase) {
    private val mutex = Mutex()
    suspend fun ensureSeeded() = mutex.withLock { database.donationDao().insertDemoDonations(records()) }
    suspend fun resetDemoOnly() = mutex.withLock {
        database.withTransaction {
            database.donationDao().deleteDemoDonations()
            database.donationDao().insertDemoDonations(records())
        }
    }
    companion object {
        const val VERSION = 1
        fun records(): List<DonationEntity> {
            val categories = listOf("تعليم", "غذاء", "أثاث", "ملابس", "علاج", "سكن")
            val offers = listOf("حقائب وكتب مدرسية", "سلال أغذية توضيحية", "طاولة ومقاعد للدراسة", "ملابس شتوية نظيفة", "مستلزمات رعاية منزلية", "مساعدة سكنية توضيحية")
            val requests = listOf("طلب كتب لطالب", "طلب سلة غذاء", "طلب أثاث أساسي", "طلب ملابس شتوية", "طلب مستلزمات رعاية", "طلب مساعدة سكنية")
            val cities = listOf("حلب", "دمشق", "حمص", "حماة", "اللاذقية", "طرطوس")
            val images = listOf("drawable/ihsan_ehsan_school_backpack", null, null, "drawable/ihsan_ehsan_winter_clothes", "drawable/ihsan_ehsan_care_support", null)
            val states = listOf("AVAILABLE", "PENDING", "COMPLETED")
            return (0 until 12).map { n ->
                val index = n % 6
                DonationEntity(id = -8_400_001L - n, title = "[DEMO] " + (if (n < 6) offers[index] else requests[index]),
                    description = "مثال تجريبي محلي خيالي لا يمثل شخصًا أو جهة حقيقية. " +
                        "يمكن استعراض تفاصيله وتصفية نتائجه لتجربة الواجهة. الصورة إن وجدت توضيحية من أصول التطبيق. " +
                        "الحالة محاكاة على الجهاز؛ لم تحدث عملية تبرع أو دفع أو تواصل. لا يوجد رقم اتصال لهذا المثال.",
                    category = categories[index], location = cities[index], type = if (n < 6) "OFFER" else "REQUEST",
                    status = states[n % 3], donorName = "ملف تجريبي ${n + 1}", phoneNumber = "", imageUrl = images[index],
                    createdAt = 1_759_305_600_000L + n * 3_600_000L, ownerId = "demo:publisher:${n + 1}", origin = "DEMO")
            }
        }
    }
}
