param(
    [Parameter(Mandatory = $true)][string]$CandidateApk,
    [ValidateSet('Data','Routes','Interactions','Migration')][string]$Suite = 'Data',
    [string]$JavaHome = 'C:\Users\molha\.jdks\jbr-21.0.3'
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$adb = 'C:\android_sdk\platform-tools\adb.exe'
$serial = 'ac190123'
$package = 'com.example.mol.referenceqa'
$preflight = & (Join-Path $PSScriptRoot 'physical_device_preflight.ps1') -CandidateApk $CandidateApk -JavaHome $JavaHome
if ($LASTEXITCODE -ne 0) { throw 'Physical preflight failed; no test launched.' }
$evidenceLine = @($preflight | Where-Object { $_ -like 'Identity verified only. Evidence: *' })
if ($evidenceLine.Count -ne 1) { throw 'Preflight did not report its evidence directory.' }
$identityDir = $evidenceLine[0].Substring('Identity verified only. Evidence: '.Length)
$identity = Get-Content (Join-Path $identityDir 'identity.json') -Raw | ConvertFrom-Json
if ($identity.rcSha256 -ne $identity.installedSha256) { throw 'Candidate and installed APK bytes differ. This runner never installs or replaces APKs.' }
$info = & $adb -s $serial shell dumpsys package $package
if ($LASTEXITCODE -ne 0 -or ($info -join "`n") -notmatch 'pkgFlags=\[[^\]]*DEBUGGABLE') {
    throw 'These diagnostics require the intentionally prepared debug QA target. Direct Release acceptance is separate.'
}
$runner = 'com.example.mol.referenceqa.test/androidx.test.runner.AndroidJUnitRunner'
$classes = switch ($Suite) {
    'Data' { 'com.example.mol.ui.ProductionStartupTest,com.example.mol.ui.ReleaseCommunityPersistenceTest,com.example.mol.ui.ReleaseContentAndPlayerTest,com.example.mol.ui.ReleaseCorePersistenceTest#canonicalEditionUpdateRetainsVerseIdAndBookmark,com.example.mol.ui.ReleaseCorePersistenceTest#realSqlDailyRolloverRetainsFavoritesAndTasbihTotal' }
    'Routes' { 'com.example.mol.ui.DarkFinalRouteMatrixTest' }
    'Interactions' { 'com.example.mol.ui.DarkFinalNativeNavigationTest,com.example.mol.ui.PhysicalCommunityJourneyTest,com.example.mol.ui.VideoAuditRegressionTest,com.example.mol.ui.ReleaseFunctionalJourneyTest#quranReaderZoomCollapseAndReciterSelectionPersistWhileRecordingsAreDisabled,com.example.mol.ui.ReleaseFunctionalJourneyTest#globalBrowseDuasOpensVerifiedLibraryInsteadOfObsoleteSampleId,com.example.mol.ui.LearningInteractionTest#searchDoesNotNavigateBeforeExplicitSubmission' }
    'Migration' {
        $runner = 'com.example.feature.test/androidx.test.runner.AndroidJUnitRunner'
        'com.example.feature.core.data.local.database.IhsanDatabaseMigrationTest'
    }
}
$instrumentation = & $adb -s $serial shell pm list instrumentation
if ($LASTEXITCODE -ne 0 -or -not (($instrumentation -join "`n").Contains("instrumentation:$runner "))) { throw 'Required instrumentation is absent. No install attempted.' }
$out = Join-Path $root ('.artifacts\physical-device-qa\acceptance-' + $Suite + '-' + [DateTimeOffset]::UtcNow.ToString('yyyyMMddTHHmmssZ'))
New-Item -ItemType Directory -Path $out -Force | Out-Null
$identity | ConvertTo-Json | Set-Content (Join-Path $out 'identity.json') -Encoding utf8
& $adb -s $serial shell am instrument -w -r -e class $classes -e physicalDeviceSerial $serial $runner *> (Join-Path $out 'instrumentation.txt')
$instrumentExit = $LASTEXITCODE
$transcript = Get-Content (Join-Path $out 'instrumentation.txt') -Raw
& $adb -s $serial logcat -d -v epoch -s AndroidRuntime:E IhsanYouTube:W IhsanPhysicalQa:I *> (Join-Path $out 'logcat.txt')
if ($instrumentExit -ne 0 -or $transcript -notmatch 'OK \(\d+ tests?\)' -or $transcript -match 'FAILURES!!!|INSTRUMENTATION_FAILED') { throw "Suite failed/interrupted. Preserve partial evidence: $out" }
Write-Output "Diagnostics complete: $out. This does not certify all Release functionality, media/audio, sensors, or reboot delivery."
