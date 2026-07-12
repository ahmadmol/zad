package com.example.feature.ihsanplus.prayerassist.data

import com.example.feature.ihsanplus.prayerassist.domain.model.*

class IhsanPlusPrayerAssistFakeDataSource {
    fun getDashboard(): IhsanPlusPrayerAssistDashboard {
        return IhsanPlusPrayerAssistDashboard(
            locationLabel = "حلب، سوريا",
            calculationMethodLabel = "رابطة العالم الإسلامي",
            madhabLabel = "الشافعي",
            nextPrayer = IhsanPlusNextPrayerInfo(
                prayerName = "المغرب",
                prayerTimeLabel = "07:45 م",
                remainingTimeLabel = "باقي ساعتان و15 دقيقة",
                isCurrentDay = true
            ),
            notificationPreferences = IhsanPlusPrayerNotificationPreferences(
                adhanEnabled = true,
                prePrayerReminderEnabled = true,
                prePrayerReminderMinutes = 15,
                iqamaReminderEnabled = false,
                iqamaReminderMinutes = 10,
                silentModeLabel = "غير مفعل"
            ),
            reminderPreviewItems = listOf(
                IhsanPlusPrayerReminderPreviewItem("1", "الفجر", "04:12 ص", "الأذان مفعل", "تنبيه قبل بـ 15 د", "تنبيه الإقامة غير مفعل"),
                IhsanPlusPrayerReminderPreviewItem("2", "الظهر", "12:30 م", "الأذان مفعل", "تنبيه قبل بـ 15 د", "تنبيه الإقامة غير مفعل"),
                IhsanPlusPrayerReminderPreviewItem("3", "العصر", "04:15 م", "الأذان مفعل", "تنبيه قبل بـ 15 د", "تنبيه الإقامة غير مفعل"),
                IhsanPlusPrayerReminderPreviewItem("4", "المغرب", "07:45 م", "الأذان مفعل", "تنبيه قبل بـ 15 د", "تنبيه الإقامة غير مفعل"),
                IhsanPlusPrayerReminderPreviewItem("5", "العشاء", "09:15 م", "الأذان مفعل", "تنبيه قبل بـ 15 د", "تنبيه الإقامة غير مفعل")
            ),
            qiblaAssist = IhsanPlusQiblaAssist(
                qiblaDirectionLabel = "جنوب شرق (135°)",
                compassAccuracyLabel = "دقة متوسطة",
                deviceSupportLabel = "مدعوم",
                offlineStatusLabel = "متاح أوفلاين",
                lastKnownLocationLabel = "منذ 5 دقائق"
            ),
            calibrationTips = listOf(
                IhsanPlusCompassCalibrationTip("1", "حركة الرقم 8", "حرك هاتفك في الهواء على شكل رقم 8 عدة مرات."),
                IhsanPlusCompassCalibrationTip("2", "الابتعاد عن المعادن", "تجنب التواجد بالقرب من الأجسام المعدنية الكبيرة أو المغناطيس."),
                IhsanPlusCompassCalibrationTip("3", "تحديث الموقع", "تأكد من تفعيل الـ GPS للحصول على أدق النتائج.")
            ),
            quickActions = listOf(
                IhsanPlusPrayerAssistQuickAction("1", "تغيير المدينة", "اختر موقعك يدوياً", IhsanPlusPrayerAssistQuickActionType.CHANGE_CITY),
                IhsanPlusPrayerAssistQuickAction("2", "إعدادات الأذان", "تخصيص أصوات التنبيه", IhsanPlusPrayerAssistQuickActionType.OPEN_PRAYER_SETTINGS),
                IhsanPlusPrayerAssistQuickAction("3", "معايرة البوصلة", "تحسين دقة القبلة", IhsanPlusPrayerAssistQuickActionType.CALIBRATE_COMPASS)
            )
        )
    }
}
