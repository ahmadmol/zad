package com.example.feature.ihsanplus.integration.adapter

import com.example.feature.core.preferences.UserPreferences
import com.example.feature.ehsan.domain.repository.EhsanRepository
import com.example.feature.ihsanplus.integration.contract.IhsanPlusCharitySource
import com.example.feature.ihsanplus.integration.model.CharityCapability
import com.example.feature.ihsanplus.integration.model.CharityListingSnapshot
import com.example.feature.ihsanplus.integration.model.IhsanPlusCharitySourceSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant

/**
 * Read-only local-board charity adapter.
 * Capability is always [CharityCapability.LocalBoard] — never BackendVerified.
 * Does not expose verification scores, trust badges, or impact totals.
 * Not registered in production DI in Part 5.
 */
class ProductionCharitySourceAdapter(
    private val ehsanRepository: EhsanRepository,
    private val userPreferences: UserPreferences
) : IhsanPlusCharitySource {

    override fun observeSnapshot(): Flow<IhsanPlusCharitySourceSnapshot> =
        combine(
            ehsanRepository.getAllDonations(),
            userPreferences.userName
        ) { donations, userName ->
            val hasLocalProfile = userName.isNotBlank() && userName != "مستخدم إحسان"
            IhsanPlusCharitySourceSnapshot(
                listings = donations.map { donation ->
                    CharityListingSnapshot(
                        id = donation.id,
                        title = donation.title,
                        type = donation.type,
                        category = donation.category,
                        location = donation.location,
                        status = donation.status
                    )
                },
                localProfileAvailable = hasLocalProfile,
                capability = CharityCapability.LocalBoard,
                generatedAt = Instant.now()
            )
        }
}
