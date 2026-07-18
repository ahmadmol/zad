package com.example.feature.ihsanplus.integration.policy

import com.example.feature.ihsanplus.integration.model.CharityCapability
import com.example.feature.ihsanplus.integration.model.IhsanPlusCapability
import com.example.feature.ihsanplus.integration.model.IhsanPlusIntegrationStatus
import com.example.feature.ihsanplus.integration.model.IhsanPlusReadinessIssue
import com.example.feature.ihsanplus.integration.model.IhsanPlusReadinessIssueCode
import com.example.feature.ihsanplus.integration.contract.IhsanPlusIntegrationReadinessEvaluator

/**
 * Trust rules for charity/IhsanPlus data. Production must never claim BackendVerified
 * without a real verification backend.
 */
object IhsanPlusDataTrustPolicy {
    fun isAllowedInProduction(capability: CharityCapability): Boolean =
        when (capability) {
            CharityCapability.LocalBoard -> true
            CharityCapability.Unavailable -> true
            CharityCapability.Demo -> false
            CharityCapability.BackendVerified -> false
        }

    fun allowsVerificationClaims(capability: CharityCapability): Boolean =
        capability == CharityCapability.BackendVerified
}

data class IhsanPlusReleasePolicyInput(
    val ihsanPlusModulesRegisteredInProduction: Boolean,
    val productionDailyAdapterAvailable: Boolean,
    val productionPrayerAdapterAvailable: Boolean,
    val productionCharityAdapterAvailable: Boolean,
    val charityCapability: CharityCapability,
    val routesRegisteredInProduction: Boolean,
    val migrationTestsPresent: Boolean,
    val deviceVerificationCompleted: Boolean
)

object IhsanPlusReleasePolicy {
    fun evaluate(input: IhsanPlusReleasePolicyInput): IhsanPlusIntegrationStatus {
        val blockers = mutableListOf<IhsanPlusReadinessIssue>()
        val warnings = mutableListOf<IhsanPlusReadinessIssue>()
        val capabilities = mutableSetOf<IhsanPlusCapability>()

        if (input.ihsanPlusModulesRegisteredInProduction) {
            blockers += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.FakeDataSourceActive,
                "IhsanPlus demo modules must not be registered in production DI"
            )
        }
        if (input.routesRegisteredInProduction) {
            blockers += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.RouteNotApproved,
                "IhsanPlus routes are not production-approved"
            )
        }
        if (!input.productionDailyAdapterAvailable) {
            blockers += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.MissingProductionAdapter,
                "Daily production adapter not available"
            )
        } else {
            capabilities += IhsanPlusCapability.DailyReadOnly
        }
        if (!input.productionPrayerAdapterAvailable) {
            blockers += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.MissingProductionAdapter,
                "Prayer production adapter not available"
            )
        } else {
            capabilities += IhsanPlusCapability.PrayerReadOnly
        }
        if (!input.productionCharityAdapterAvailable) {
            blockers += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.MissingProductionAdapter,
                "Charity production adapter not available"
            )
        } else when (input.charityCapability) {
            CharityCapability.LocalBoard -> capabilities += IhsanPlusCapability.CharityLocalBoard
            CharityCapability.Demo -> {
                capabilities += IhsanPlusCapability.CharityDemoOnly
                blockers += IhsanPlusReadinessIssue(
                    IhsanPlusReadinessIssueCode.CharityTrustUnsupported,
                    "Demo charity capability cannot ship as production trust UI"
                )
            }
            CharityCapability.BackendVerified -> {
                capabilities += IhsanPlusCapability.CharityBackendVerified
                blockers += IhsanPlusReadinessIssue(
                    IhsanPlusReadinessIssueCode.BackendRequired,
                    "BackendVerified requires an approved verification backend"
                )
            }
            CharityCapability.Unavailable -> {
                warnings += IhsanPlusReadinessIssue(
                    IhsanPlusReadinessIssueCode.UnknownDataOwnership,
                    "Charity capability unavailable"
                )
            }
        }
        if (!IhsanPlusDataTrustPolicy.isAllowedInProduction(input.charityCapability) &&
            input.charityCapability != CharityCapability.Unavailable
        ) {
            blockers += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.ReleasePolicyViolation,
                "Charity capability ${input.charityCapability} violates production trust policy"
            )
        }
        if (!input.migrationTestsPresent) {
            warnings += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.MigrationTestsMissing,
                "Room migration coverage incomplete"
            )
        }
        if (!input.deviceVerificationCompleted) {
            warnings += IhsanPlusReadinessIssue(
                IhsanPlusReadinessIssueCode.DeviceVerificationMissing,
                "Device verification not completed"
            )
        }

        return IhsanPlusIntegrationStatus(
            ready = blockers.isEmpty(),
            capabilities = capabilities,
            blockers = blockers.distinctBy { it.code to it.message },
            warnings = warnings
        )
    }
}

/**
 * Deterministic evaluator for Part 5. Reflects current tree: adapters may exist as code,
 * but DI/routes remain unwired and charity remains LocalBoard-only.
 */
class DefaultIhsanPlusIntegrationReadinessEvaluator(
    private val input: IhsanPlusReleasePolicyInput
) : IhsanPlusIntegrationReadinessEvaluator {
    override suspend fun evaluate(): IhsanPlusIntegrationStatus =
        IhsanPlusReleasePolicy.evaluate(input)
}
