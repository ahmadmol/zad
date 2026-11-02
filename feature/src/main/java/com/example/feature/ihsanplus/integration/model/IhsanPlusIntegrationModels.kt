package com.example.feature.ihsanplus.integration.model

import com.example.feature.prayer.domain.model.NextPrayer
import com.example.feature.prayer.domain.model.PrayerDay
import com.example.feature.prayer.domain.model.PrayerLocationState
import com.example.feature.prayer.domain.model.PrayerSystemStatus
import java.time.Instant

/** Production-neutral daily snapshot for future IhsanPlus integration. */
data class IhsanPlusDailySourceSnapshot(
    val profileDisplayName: String?,
    val nextPrayerNameArabic: String?,
    val nextPrayerEpochMillis: Long?,
    val quranSurahId: Int?,
    val quranAyahNumber: Int?,
    val dhikrTodayCount: Int,
    val activityIds: List<String>,
    val completedActivityIds: List<String>,
    val generatedAt: Instant
)

data class IhsanPlusPrayerSourceSnapshot(
    val prayerDay: PrayerDay?,
    val nextPrayer: NextPrayer?,
    val locationState: PrayerLocationState,
    val systemStatus: PrayerSystemStatus,
    val generatedAt: Instant
)

enum class CharityCapability {
    LocalBoard,
    Demo,
    BackendVerified,
    Unavailable
}

data class CharityListingSnapshot(
    val id: Long,
    val title: String,
    val type: String,
    val category: String,
    val location: String,
    val status: String
)

data class IhsanPlusCharitySourceSnapshot(
    val listings: List<CharityListingSnapshot>,
    val localProfileAvailable: Boolean,
    val capability: CharityCapability,
    val generatedAt: Instant
)

enum class IhsanPlusCapability {
    DailyReadOnly,
    PrayerReadOnly,
    CharityLocalBoard,
    CharityDemoOnly,
    CharityBackendVerified
}

enum class IhsanPlusReadinessIssueCode {
    FakeDataSourceActive,
    MissingProductionAdapter,
    RouteNotApproved,
    DiNotApproved,
    CharityTrustUnsupported,
    BackendRequired,
    MigrationTestsMissing,
    DeviceVerificationMissing,
    ReleasePolicyViolation,
    UnknownDataOwnership
}

data class IhsanPlusReadinessIssue(
    val code: IhsanPlusReadinessIssueCode,
    val message: String
)

data class IhsanPlusIntegrationStatus(
    val ready: Boolean,
    val capabilities: Set<IhsanPlusCapability>,
    val blockers: List<IhsanPlusReadinessIssue>,
    val warnings: List<IhsanPlusReadinessIssue>
)
