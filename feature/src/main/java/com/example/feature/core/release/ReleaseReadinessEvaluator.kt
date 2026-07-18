package com.example.feature.core.release

/**
 * Deterministic release-readiness model for Part 5 gates.
 * Not exposed as a production screen.
 */
data class ReleaseReadinessStatus(
    val ready: Boolean,
    val blockers: List<ReleaseBlocker>,
    val warnings: List<ReleaseWarning>
)

enum class ReleaseBlocker {
    ApplicationIdUnapproved,
    ReleaseSigningMissing,
    ReleaseBuildFailed,
    MigrationCoverageIncomplete,
    CriticalTestsFailed,
    IhsanPlusFakeWired,
    BackupPolicyInvalid,
    ExportedComponentUnsafe,
    DeviceVerificationMissing,
    PrivacyPolicyMissing
}

enum class ReleaseWarning {
    MinifyDisabled,
    ObservabilityVendorPending,
    ApplicationIdIsExample,
    InstrumentedTestsNotRun
}

data class ReleaseReadinessInput(
    val applicationIdApproved: Boolean,
    val releaseSigningConfigured: Boolean,
    val releaseBuildSucceeded: Boolean,
    val migrationCoverageComplete: Boolean,
    val criticalTestsPassed: Boolean,
    val ihsanPlusFakeWiredInProduction: Boolean,
    val backupPolicySafe: Boolean,
    val exportedComponentsSafe: Boolean,
    val deviceVerificationCompleted: Boolean,
    val privacyPolicyPresent: Boolean,
    val minifyEnabled: Boolean,
    val observabilityVendorApproved: Boolean
)

object ReleaseReadinessEvaluator {
    fun evaluate(input: ReleaseReadinessInput): ReleaseReadinessStatus {
        val blockers = mutableListOf<ReleaseBlocker>()
        val warnings = mutableListOf<ReleaseWarning>()

        if (!input.applicationIdApproved) blockers += ReleaseBlocker.ApplicationIdUnapproved
        if (!input.releaseSigningConfigured) blockers += ReleaseBlocker.ReleaseSigningMissing
        if (!input.releaseBuildSucceeded) blockers += ReleaseBlocker.ReleaseBuildFailed
        if (!input.migrationCoverageComplete) blockers += ReleaseBlocker.MigrationCoverageIncomplete
        if (!input.criticalTestsPassed) blockers += ReleaseBlocker.CriticalTestsFailed
        if (input.ihsanPlusFakeWiredInProduction) blockers += ReleaseBlocker.IhsanPlusFakeWired
        if (!input.backupPolicySafe) blockers += ReleaseBlocker.BackupPolicyInvalid
        if (!input.exportedComponentsSafe) blockers += ReleaseBlocker.ExportedComponentUnsafe
        if (!input.deviceVerificationCompleted) blockers += ReleaseBlocker.DeviceVerificationMissing
        if (!input.privacyPolicyPresent) blockers += ReleaseBlocker.PrivacyPolicyMissing

        if (!input.minifyEnabled) warnings += ReleaseWarning.MinifyDisabled
        if (!input.observabilityVendorApproved) warnings += ReleaseWarning.ObservabilityVendorPending
        if (!input.applicationIdApproved) warnings += ReleaseWarning.ApplicationIdIsExample
        if (!input.deviceVerificationCompleted) warnings += ReleaseWarning.InstrumentedTestsNotRun

        return ReleaseReadinessStatus(
            ready = blockers.isEmpty(),
            blockers = blockers.distinct(),
            warnings = warnings.distinct()
        )
    }
}
