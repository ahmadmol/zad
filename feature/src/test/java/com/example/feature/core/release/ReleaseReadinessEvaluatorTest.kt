package com.example.feature.core.release

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseReadinessEvaluatorTest {

    private fun base(
        applicationIdApproved: Boolean = true,
        releaseSigningConfigured: Boolean = true,
        releaseBuildSucceeded: Boolean = true,
        migrationCoverageComplete: Boolean = true,
        criticalTestsPassed: Boolean = true,
        ihsanPlusFakeWiredInProduction: Boolean = false,
        backupPolicySafe: Boolean = true,
        exportedComponentsSafe: Boolean = true,
        deviceVerificationCompleted: Boolean = true,
        privacyPolicyPresent: Boolean = true,
        minifyEnabled: Boolean = true,
        observabilityVendorApproved: Boolean = true
    ) = ReleaseReadinessInput(
        applicationIdApproved = applicationIdApproved,
        releaseSigningConfigured = releaseSigningConfigured,
        releaseBuildSucceeded = releaseBuildSucceeded,
        migrationCoverageComplete = migrationCoverageComplete,
        criticalTestsPassed = criticalTestsPassed,
        ihsanPlusFakeWiredInProduction = ihsanPlusFakeWiredInProduction,
        backupPolicySafe = backupPolicySafe,
        exportedComponentsSafe = exportedComponentsSafe,
        deviceVerificationCompleted = deviceVerificationCompleted,
        privacyPolicyPresent = privacyPolicyPresent,
        minifyEnabled = minifyEnabled,
        observabilityVendorApproved = observabilityVendorApproved
    )

    @Test
    fun `ready when all gates pass`() {
        val status = ReleaseReadinessEvaluator.evaluate(base())
        assertTrue(status.ready)
        assertTrue(status.blockers.isEmpty())
    }

    @Test
    fun `each blocker is reported`() {
        ReleaseBlocker.entries.forEach { blocker ->
            val input = when (blocker) {
                ReleaseBlocker.ApplicationIdUnapproved -> base(applicationIdApproved = false)
                ReleaseBlocker.ReleaseSigningMissing -> base(releaseSigningConfigured = false)
                ReleaseBlocker.ReleaseBuildFailed -> base(releaseBuildSucceeded = false)
                ReleaseBlocker.MigrationCoverageIncomplete -> base(migrationCoverageComplete = false)
                ReleaseBlocker.CriticalTestsFailed -> base(criticalTestsPassed = false)
                ReleaseBlocker.IhsanPlusFakeWired -> base(ihsanPlusFakeWiredInProduction = true)
                ReleaseBlocker.BackupPolicyInvalid -> base(backupPolicySafe = false)
                ReleaseBlocker.ExportedComponentUnsafe -> base(exportedComponentsSafe = false)
                ReleaseBlocker.DeviceVerificationMissing -> base(deviceVerificationCompleted = false)
                ReleaseBlocker.PrivacyPolicyMissing -> base(privacyPolicyPresent = false)
            }
            val status = ReleaseReadinessEvaluator.evaluate(input)
            assertFalse(status.ready)
            assertTrue(status.blockers.contains(blocker))
        }
    }

    @Test
    fun `multiple blockers accumulate`() {
        val status = ReleaseReadinessEvaluator.evaluate(
            base(
                applicationIdApproved = false,
                releaseSigningConfigured = false,
                ihsanPlusFakeWiredInProduction = true
            )
        )
        assertEquals(3, status.blockers.size)
        assertTrue(status.blockers.contains(ReleaseBlocker.ApplicationIdUnapproved))
        assertTrue(status.blockers.contains(ReleaseBlocker.ReleaseSigningMissing))
        assertTrue(status.blockers.contains(ReleaseBlocker.IhsanPlusFakeWired))
    }

    @Test
    fun `warning only state remains ready`() {
        val status = ReleaseReadinessEvaluator.evaluate(
            base(minifyEnabled = false, observabilityVendorApproved = false)
        )
        assertTrue(status.ready)
        assertTrue(status.warnings.contains(ReleaseWarning.MinifyDisabled))
        assertTrue(status.warnings.contains(ReleaseWarning.ObservabilityVendorPending))
    }

    @Test
    fun `fake ihsanplus wiring is a hard blocker`() {
        val status = ReleaseReadinessEvaluator.evaluate(base(ihsanPlusFakeWiredInProduction = true))
        assertFalse(status.ready)
        assertEquals(listOf(ReleaseBlocker.IhsanPlusFakeWired), status.blockers)
    }
}
