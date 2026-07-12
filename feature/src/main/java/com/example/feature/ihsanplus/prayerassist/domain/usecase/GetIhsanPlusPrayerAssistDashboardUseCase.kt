package com.example.feature.ihsanplus.prayerassist.domain.usecase

import com.example.feature.ihsanplus.prayerassist.data.IhsanPlusPrayerAssistFakeDataSource
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerAssistDashboard

class GetIhsanPlusPrayerAssistDashboardUseCase(
    private val fakeDataSource: IhsanPlusPrayerAssistFakeDataSource = IhsanPlusPrayerAssistFakeDataSource()
) {
    operator fun invoke(): IhsanPlusPrayerAssistDashboard {
        return fakeDataSource.getDashboard()
    }
}
