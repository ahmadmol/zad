package com.example.feature.ihsanplus.prayerassist.domain.usecase

import com.example.feature.ihsanplus.prayerassist.data.DemoIhsanPlusPrayerAssistDataSource
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerAssistDashboard

class GetIhsanPlusPrayerAssistDashboardUseCase(
    private val demoDataSource: DemoIhsanPlusPrayerAssistDataSource = DemoIhsanPlusPrayerAssistDataSource()
) {
    operator fun invoke(): IhsanPlusPrayerAssistDashboard {
        return demoDataSource.getDashboard()
    }
}
