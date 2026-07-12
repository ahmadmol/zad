package com.example.feature.ihsanplus.charitytrust.domain.usecase

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityCasePreview
import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusVerificationStatus

class EvaluateIhsanPlusCaseTrustLevelUseCase {
    operator fun invoke(case: IhsanPlusCharityCasePreview): String {
        return when (case.verificationStatus) {
            IhsanPlusVerificationStatus.VERIFIED -> "موثوقة"
            IhsanPlusVerificationStatus.PENDING_REVIEW -> "قيد المراجعة"
            IhsanPlusVerificationStatus.NEEDS_MORE_INFO -> "تحتاج معلومات إضافية"
            IhsanPlusVerificationStatus.UNVERIFIED -> "غير موثقة"
        }
    }
}
