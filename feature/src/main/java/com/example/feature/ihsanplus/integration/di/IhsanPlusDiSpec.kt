package com.example.feature.ihsanplus.integration.di

enum class BindingScope {
    DebugOnly,
    ReleaseForbidden,
    FutureProduction
}

data class IhsanPlusBindingSpec(
    val contractName: String,
    val requiredScope: BindingScope,
    val productionImplementationRequired: Boolean,
    val fakeAllowedInRelease: Boolean
)

/**
 * Non-runtime DI specification. Do not register these bindings in production Koin.
 */
object IhsanPlusDiSpec {
    val bindings: List<IhsanPlusBindingSpec> = listOf(
        IhsanPlusBindingSpec(
            contractName = "IhsanPlusDailySource",
            requiredScope = BindingScope.FutureProduction,
            productionImplementationRequired = true,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "IhsanPlusPrayerSource",
            requiredScope = BindingScope.FutureProduction,
            productionImplementationRequired = true,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "IhsanPlusCharitySource",
            requiredScope = BindingScope.FutureProduction,
            productionImplementationRequired = true,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "IhsanPlusIntegrationReadinessEvaluator",
            requiredScope = BindingScope.FutureProduction,
            productionImplementationRequired = true,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "DemoIhsanPlusDailyDataSource",
            requiredScope = BindingScope.DebugOnly,
            productionImplementationRequired = false,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "DemoIhsanPlusCharityTrustDataSource",
            requiredScope = BindingScope.ReleaseForbidden,
            productionImplementationRequired = false,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "ihsanPlusDailyModule",
            requiredScope = BindingScope.ReleaseForbidden,
            productionImplementationRequired = false,
            fakeAllowedInRelease = false
        ),
        IhsanPlusBindingSpec(
            contractName = "ihsanPlusCharityTrustModule",
            requiredScope = BindingScope.ReleaseForbidden,
            productionImplementationRequired = false,
            fakeAllowedInRelease = false
        )
    )

    val forbiddenReleaseModuleNames: Set<String> = setOf(
        "ihsanPlusDailyModule",
        "ihsanPlusCharityTrustModule"
    )
}
