package com.example.feature.ihsanplus.charitytrust.domain.usecase

import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusTrustSummary

class BuildIhsanPlusTrustMessageUseCase {
    operator fun invoke(summary: IhsanPlusTrustSummary): String {
        return "تم توثيق ${summary.verifiedCasesCountLabel} بنجاح. نحن نعمل على مراجعة الحالات المتبقية لضمان أعلى مستويات الأمان."
    }
}
