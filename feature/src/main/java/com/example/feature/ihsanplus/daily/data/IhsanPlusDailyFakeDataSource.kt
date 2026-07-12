package com.example.feature.ihsanplus.daily.data

import com.example.feature.ihsanplus.daily.domain.model.*

class IhsanPlusDailyFakeDataSource {
    fun getDailyExperience(): IhsanPlusDailyExperience {
        return IhsanPlusDailyExperience(
            greetingName = "أهلاً بك يا محسن",
            hijriDateLabel = "14 محرم 1446",
            gregorianDateLabel = "20 يوليو 2024",
            nextPrayer = IhsanPlusPrayerSummary(
                prayerName = "الظهر",
                prayerTimeLabel = "12:30 م",
                remainingTimeLabel = "باقي 45 دقيقة",
                locationLabel = "حلب، سوريا",
                isNotificationEnabled = true
            ),
            quranProgress = IhsanPlusQuranProgress(
                surahName = "البقرة",
                ayahLabel = "الآية 255",
                progressPercent = 12,
                lastReadLabel = "منذ ساعتين"
            ),
            dailyDua = IhsanPlusDailyDua(
                title = "دعاء الصباح",
                content = "اللهم بك أصبحنا وبك أمسينا وبك نحيا وبك نموت وإليك النشور",
                source = "حصن المسلم"
            ),
            dailyHadith = IhsanPlusDailyHadith(
                title = "في فضل العمل",
                content = "إن الله يحب إذا عمل أحدكم عملاً أن يتقنه",
                source = "صحيح الجامع"
            ),
            dhikrProgress = IhsanPlusDhikrProgress(
                title = "سبحان الله وبحمده",
                currentCount = 45,
                targetCount = 100
            ),
            charitySuggestion = IhsanPlusCharitySuggestion(
                title = "كفالة يتيم",
                description = "ساهم في توفير احتياجات طفل يتيم في مدينتك",
                categoryLabel = "رعاية أيتام",
                urgencyLabel = "عاجل"
            ),
            activityItems = listOf(
                IhsanPlusDailyActivityItem(
                    id = "1",
                    title = "الصلوات الخمس",
                    subtitle = "تم أداء 3 صلوات",
                    progressPercent = 60,
                    type = IhsanPlusDailyActivityType.PRAYER
                ),
                IhsanPlusDailyActivityItem(
                    id = "2",
                    title = "ورد القرآن",
                    subtitle = "قرأت 5 صفحات",
                    progressPercent = 50,
                    type = IhsanPlusDailyActivityType.QURAN
                )
            )
        )
    }
}
