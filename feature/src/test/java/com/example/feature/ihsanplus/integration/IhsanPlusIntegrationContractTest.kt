package com.example.feature.ihsanplus.integration

import com.example.feature.ihsanplus.integration.di.BindingScope
import com.example.feature.ihsanplus.integration.di.IhsanPlusDiSpec
import com.example.feature.ihsanplus.integration.model.CharityCapability
import com.example.feature.ihsanplus.integration.model.IhsanPlusReadinessIssueCode
import com.example.feature.ihsanplus.integration.policy.IhsanPlusDataTrustPolicy
import com.example.feature.ihsanplus.integration.policy.IhsanPlusReleasePolicy
import com.example.feature.ihsanplus.integration.policy.IhsanPlusReleasePolicyInput
import com.example.feature.ihsanplus.integration.routing.IhsanPlusRouteSpec
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IhsanPlusIntegrationContractTest {

    @Test
    fun `all route specs are unapproved for production`() {
        assertTrue(IhsanPlusRouteSpec.ALL.all { !it.productionApproved })
        assertEquals("ihsan_plus_daily", IhsanPlusRouteSpec.Daily.route)
        assertEquals("ihsan_plus_prayer_assist", IhsanPlusRouteSpec.PrayerAssist.route)
        assertEquals("ihsan_plus_charity_trust", IhsanPlusRouteSpec.CharityTrust.route)
    }

    @Test
    fun `charity trust route requires backend verified capability`() {
        assertTrue(
            IhsanPlusRouteSpec.CharityTrust.requiredCapabilities.any {
                it.name == "CharityBackendVerified"
            }
        )
    }

    @Test
    fun `demo fake modules are forbidden in release DI spec`() {
        assertTrue(IhsanPlusDiSpec.forbiddenReleaseModuleNames.contains("ihsanPlusDailyModule"))
        assertTrue(IhsanPlusDiSpec.forbiddenReleaseModuleNames.contains("ihsanPlusCharityTrustModule"))
        assertTrue(
            IhsanPlusDiSpec.bindings
                .filter { it.contractName.contains("Demo") || it.contractName.contains("Module") }
                .all { !it.fakeAllowedInRelease }
        )
        assertTrue(
            IhsanPlusDiSpec.bindings.any {
                it.contractName == "DemoIhsanPlusCharityTrustDataSource" &&
                    it.requiredScope == BindingScope.ReleaseForbidden
            }
        )
    }

    @Test
    fun `data trust policy rejects demo and backend verified for production`() {
        assertTrue(IhsanPlusDataTrustPolicy.isAllowedInProduction(CharityCapability.LocalBoard))
        assertFalse(IhsanPlusDataTrustPolicy.isAllowedInProduction(CharityCapability.Demo))
        assertFalse(IhsanPlusDataTrustPolicy.isAllowedInProduction(CharityCapability.BackendVerified))
        assertFalse(IhsanPlusDataTrustPolicy.allowsVerificationClaims(CharityCapability.LocalBoard))
        assertTrue(IhsanPlusDataTrustPolicy.allowsVerificationClaims(CharityCapability.BackendVerified))
    }

    @Test
    fun `readiness evaluator blocks fake modules and unapproved routes`() {
        val status = IhsanPlusReleasePolicy.evaluate(
            IhsanPlusReleasePolicyInput(
                ihsanPlusModulesRegisteredInProduction = true,
                productionDailyAdapterAvailable = true,
                productionPrayerAdapterAvailable = true,
                productionCharityAdapterAvailable = true,
                charityCapability = CharityCapability.LocalBoard,
                routesRegisteredInProduction = true,
                migrationTestsPresent = true,
                deviceVerificationCompleted = false
            )
        )
        assertFalse(status.ready)
        assertTrue(status.blockers.any { it.code == IhsanPlusReadinessIssueCode.FakeDataSourceActive })
        assertTrue(status.blockers.any { it.code == IhsanPlusReadinessIssueCode.RouteNotApproved })
        assertTrue(status.warnings.any { it.code == IhsanPlusReadinessIssueCode.DeviceVerificationMissing })
    }

    @Test
    fun `readiness ready when adapters exist local board and no production wiring`() {
        val status = IhsanPlusReleasePolicy.evaluate(
            IhsanPlusReleasePolicyInput(
                ihsanPlusModulesRegisteredInProduction = false,
                productionDailyAdapterAvailable = true,
                productionPrayerAdapterAvailable = true,
                productionCharityAdapterAvailable = true,
                charityCapability = CharityCapability.LocalBoard,
                routesRegisteredInProduction = false,
                migrationTestsPresent = true,
                deviceVerificationCompleted = true
            )
        )
        assertTrue(status.ready)
        assertTrue(status.blockers.isEmpty())
    }

    @Test
    fun `demo charity capability blocks trust shipping`() {
        val status = IhsanPlusReleasePolicy.evaluate(
            IhsanPlusReleasePolicyInput(
                ihsanPlusModulesRegisteredInProduction = false,
                productionDailyAdapterAvailable = true,
                productionPrayerAdapterAvailable = true,
                productionCharityAdapterAvailable = true,
                charityCapability = CharityCapability.Demo,
                routesRegisteredInProduction = false,
                migrationTestsPresent = true,
                deviceVerificationCompleted = true
            )
        )
        assertFalse(status.ready)
        assertTrue(status.blockers.any { it.code == IhsanPlusReadinessIssueCode.CharityTrustUnsupported })
    }
}
