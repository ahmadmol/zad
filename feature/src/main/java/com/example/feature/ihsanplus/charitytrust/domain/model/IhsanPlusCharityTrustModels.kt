package com.example.feature.ihsanplus.charitytrust.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class IhsanPlusCharityTrustDashboard(
    val title: String,
    val subtitle: String,
    val highlightedCase: IhsanPlusCharityCasePreview,
    val trustSummary: IhsanPlusTrustSummary,
    val verificationItems: List<IhsanPlusVerificationItem>,
    val transparencyItems: List<IhsanPlusTransparencyItem>,
    val privacyGuidelines: List<IhsanPlusBeneficiaryPrivacyGuideline>,
    val safetyGuidelines: List<IhsanPlusContactSafetyGuideline>,
    val impactItems: List<IhsanPlusDonationImpactItem>,
    val quickActions: List<IhsanPlusCharityTrustQuickAction>
)

@Serializable
data class IhsanPlusCharityCasePreview(
    val id: String,
    val title: String,
    val description: String,
    val categoryLabel: String,
    val locationLabel: String,
    val urgencyLabel: String,
    val verificationStatus: IhsanPlusVerificationStatus,
    val requestedAmountLabel: String,
    val collectedAmountLabel: String,
    val progressPercent: Int,
    val contactSafetyLabel: String
)

@Serializable
data class IhsanPlusTrustSummary(
    val verifiedCasesCountLabel: String,
    val pendingReviewCountLabel: String,
    val completedCasesCountLabel: String,
    val trustMessage: String
)

@Serializable
data class IhsanPlusVerificationItem(
    val id: String,
    val title: String,
    val description: String,
    val status: IhsanPlusVerificationStatus
)

@Serializable
data class IhsanPlusTransparencyItem(
    val id: String,
    val title: String,
    val description: String,
    val valueLabel: String
)

@Serializable
data class IhsanPlusBeneficiaryPrivacyGuideline(
    val id: String,
    val title: String,
    val description: String
)

@Serializable
data class IhsanPlusContactSafetyGuideline(
    val id: String,
    val title: String,
    val description: String
)

@Serializable
data class IhsanPlusDonationImpactItem(
    val id: String,
    val title: String,
    val description: String,
    val impactLabel: String
)

@Serializable
data class IhsanPlusCharityTrustQuickAction(
    val id: String,
    val title: String,
    val subtitle: String,
    val type: IhsanPlusCharityTrustQuickActionType
)

@Serializable
enum class IhsanPlusVerificationStatus {
    VERIFIED,
    PENDING_REVIEW,
    NEEDS_MORE_INFO,
    UNVERIFIED
}

@Serializable
enum class IhsanPlusCharityTrustQuickActionType {
    VIEW_VERIFIED_CASES,
    ADD_HELP_REQUEST,
    ADD_DONATION_OFFER,
    REVIEW_SAFETY_GUIDE,
    VIEW_IMPACT_REPORT
}
