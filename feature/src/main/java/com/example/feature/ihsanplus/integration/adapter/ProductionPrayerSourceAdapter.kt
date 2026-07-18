package com.example.feature.ihsanplus.integration.adapter

import com.example.feature.ihsanplus.integration.contract.IhsanPlusPrayerSource
import com.example.feature.ihsanplus.integration.model.IhsanPlusPrayerSourceSnapshot
import com.example.feature.prayer.domain.facade.PrayerTimesFacade
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant

/**
 * Read-only production adapter over Part 1 [PrayerTimesFacade].
 * Does not calculate prayer times. Not registered in production DI in Part 5.
 */
class ProductionPrayerSourceAdapter(
    private val prayerFacade: PrayerTimesFacade
) : IhsanPlusPrayerSource {

    override fun observeSnapshot(): Flow<IhsanPlusPrayerSourceSnapshot> =
        combine(
            prayerFacade.prayerDay,
            prayerFacade.nextPrayer,
            prayerFacade.locationState,
            prayerFacade.systemStatus
        ) { day, next, location, status ->
            IhsanPlusPrayerSourceSnapshot(
                prayerDay = day,
                nextPrayer = next,
                locationState = location,
                systemStatus = status,
                generatedAt = Instant.now()
            )
        }
}
