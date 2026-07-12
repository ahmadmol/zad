package com.example.feature.ihsanplus.prayerassist.domain.usecase

import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerNotificationPreferences
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerReminderPreviewItem

class BuildIhsanPlusPrayerReminderPreviewUseCase {
    operator fun invoke(
        preferences: IhsanPlusPrayerNotificationPreferences,
        items: List<IhsanPlusPrayerReminderPreviewItem>
    ): List<String> {
        return items.map { item ->
            buildString {
                append("صلاة ${item.prayerName}: ")
                if (preferences.adhanEnabled) append("الأذان مفعّل. ") else append("الأذان صامت. ")
                if (preferences.prePrayerReminderEnabled) {
                    append("تنبيه قبل بـ ${preferences.prePrayerReminderMinutes} دقيقة. ")
                }
                if (preferences.iqamaReminderEnabled) {
                    append("تنبيه الإقامة بعد بـ ${preferences.iqamaReminderMinutes} دقيقة. ")
                }
            }
        }
    }
}
