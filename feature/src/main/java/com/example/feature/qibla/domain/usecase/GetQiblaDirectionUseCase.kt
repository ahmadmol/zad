package com.example.feature.qibla.domain.usecase

import com.example.feature.qibla.util.QiblaManager

class GetQiblaDirectionUseCase {
    operator fun invoke(userLat: Double, userLng: Double): Float {
        return QiblaManager.calculateQiblaDirection(userLat, userLng)
    }
}
