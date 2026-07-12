package com.example.feature.ihsanplus.prayerassist.domain.usecase

import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusCompassCalibrationTip
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerAssistDashboard

class GetIhsanPlusQiblaCalibrationTipsUseCase {
    operator fun invoke(dashboard: IhsanPlusPrayerAssistDashboard): List<IhsanPlusCompassCalibrationTip> {
        return dashboard.calibrationTips
    }
}
