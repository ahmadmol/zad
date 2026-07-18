package com.example.feature.ihsanplus.charitytrust.data

import com.example.feature.ihsanplus.charitytrust.domain.model.*

object IhsanPlusCharityTrustFakeDataSource {
    fun getDashboard(): IhsanPlusCharityTrustDashboard {
        return IhsanPlusCharityTrustDashboard(
            title = "طبقة الثقة في إحسان",
            subtitle = "نحن نضمن وصول تبرعاتكم لمستحقيها من خلال عملية تدقيق شاملة",
            highlightedCase = IhsanPlusCharityCasePreview(
                id = "case_1",
                title = "كفالة أيتام في القدس",
                description = "توفير الرعاية الشاملة لـ ٥٠ طفلاً فَقَدوا مُعيلهم، تشمل التعليم والغذاء.",
                categoryLabel = "كفالة أيتام",
                locationLabel = "فلسطين، القدس",
                urgencyLabel = "عاجل",
                verificationStatus = IhsanPlusVerificationStatus.VERIFIED,
                requestedAmountLabel = "١٠,٠٠٠ د.أ",
                collectedAmountLabel = "٤,٥٠٠ د.أ",
                progressPercent = 45,
                contactSafetyLabel = "اتصال آمن عبر المنصة"
            ),
            trustSummary = IhsanPlusTrustSummary(
                verifiedCasesCountLabel = "١٢٥ حالة موثقة",
                pendingReviewCountLabel = "٨ حالات تحت المراجعة",
                completedCasesCountLabel = "٣٤٠ حالة مكتملة",
                trustMessage = "نظام إحسان التقني يضمن شفافية كاملة لكل قرش يتم التبرع به."
            ),
            verificationItems = listOf(
                IhsanPlusVerificationItem("v1", "فحص الهوية", "تم التحقق من الوثائق الرسمية لمقدم الطلب", IhsanPlusVerificationStatus.VERIFIED),
                IhsanPlusVerificationItem("v2", "الزيارة الميدانية", "قام فريقنا بزيارة الموقع والتأكد من الحالة", IhsanPlusVerificationStatus.VERIFIED),
                IhsanPlusVerificationItem("v3", "التدقيق المالي", "مراجعة فواتير الاحتياجات والمصاريف", IhsanPlusVerificationStatus.VERIFIED)
            ),
            transparencyItems = listOf(
                IhsanPlusTransparencyItem("t1", "رسوم المنصة", "لا يتم اقتطاع أي رسوم من التبرع الأساسي", "٠٪"),
                IhsanPlusTransparencyItem("t2", "سرعة التحويل", "يصل التبرع للمستفيد خلال ٤٨ ساعة", "سريع"),
                IhsanPlusTransparencyItem("t3", "تقارير الأثر", "تصلك صور وفيديوهات لنتائج تبرعك", "متاح")
            ),
            privacyGuidelines = listOf(
                IhsanPlusBeneficiaryPrivacyGuideline("p1", "حماية الهوية", "لا يتم نشر صور الوجوه للمستفيدين حفاظاً على الكرامة"),
                IhsanPlusBeneficiaryPrivacyGuideline("p2", "تشفير البيانات", "جميع بيانات المستفيدين مخزنة بشكل مشفر وآمن")
            ),
            safetyGuidelines = listOf(
                IhsanPlusContactSafetyGuideline("s1", "الوساطة الآمنة", "التواصل مع المستفيد يتم عبر فريق إحسان فقط"),
                IhsanPlusContactSafetyGuideline("s2", "منع التحرش", "سياسات صارمة لحماية الداعم والمستفيد من أي مضايقات")
            ),
            impactItems = listOf(
                IhsanPlusDonationImpactItem("i1", "الأثر التعليمي", "المساهمة في بناء جيل متعلم وقادر", "مرتفع"),
                IhsanPlusDonationImpactItem("i2", "الأثر الصحي", "إنقاذ حياة وتخفيف آلام المرضى", "مباشر")
            ),
            quickActions = listOf(
                IhsanPlusCharityTrustQuickAction("a1", "تصفح الحالات الموثقة", "شاهد الحالات التي اجتازت الفحص", IhsanPlusCharityTrustQuickActionType.VIEW_VERIFIED_CASES),
                IhsanPlusCharityTrustQuickAction("a2", "طلب مساعدة", "قدم طلبك للمراجعة والتدقيق", IhsanPlusCharityTrustQuickActionType.ADD_HELP_REQUEST),
                IhsanPlusCharityTrustQuickAction("a3", "تقرير الأثر السنوي", "كيف غيرت تبرعاتكم حياة الآخرين", IhsanPlusCharityTrustQuickActionType.VIEW_IMPACT_REPORT)
            )
        )
    }
}
