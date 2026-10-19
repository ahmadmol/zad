param([switch]$SkipInstall, [switch]$MatrixOnly, [switch]$MediaOnly, [switch]$CoreOnly)
$ErrorActionPreference='Stop'
$adb='C:\android_sdk\platform-tools\adb.exe'
$device='emulator-5570'
$base=Join-Path $PSScriptRoot '..\.artifacts\video-audit-20261015'
$runner='com.example.mol.referenceqa.test/androidx.test.runner.AndroidJUnitRunner'
New-Item -ItemType Directory -Force "$base\runtime" | Out-Null
function Run-Audit([string]$name,[string]$testClass,[string[]]$extra=@()) {
    $focus=(& $adb -s $device shell dumpsys window | Select-String 'mCurrentFocus') -join ''
    if($focus -match 'Application Not Responding: com.android.systemui') {
        & $adb -s $device shell uiautomator dump /sdcard/audit-known-systemui.xml | Out-Null
        [xml]$dialog=(& $adb -s $device shell cat /sdcard/audit-known-systemui.xml) -join ''
        $title=$dialog.SelectSingleNode("//node[@resource-id='android:id/alertTitle']")
        $close=$dialog.SelectSingleNode("//node[@text='Close app']")
        if($title.text -ne "System UI isn't responding" -or $null -eq $close) { throw 'Unrecognized system dialog; do not dismiss' }
        & $adb -s $device shell screencap -p /sdcard/audit-known-systemui.png
        & $adb -s $device pull /sdcard/audit-known-systemui.png "$base\runtime\systemui-before-$name.png" | Out-Null
        $coords=[regex]::Matches($close.bounds,'\d+') | ForEach-Object { [int]$_.Value }
        & $adb -s $device shell input tap ([int](($coords[0]+$coords[2])/2)) ([int](($coords[1]+$coords[3])/2))
        "DISMISSED verified System UI ANR before $name" | Add-Content "$base\runtime\progress.txt"
        Start-Sleep -Seconds 2
    }
    "START $name $(Get-Date -Format o)" | Add-Content "$base\runtime\progress.txt"
    $selector=if([string]::IsNullOrWhiteSpace($testClass)) { @() } else { @('-e','class',$testClass) }
    & $adb -s $device shell am instrument -w -r @selector @extra $runner *> "$base\runtime\$name.log"
    $passed=Select-String -LiteralPath "$base\runtime\$name.log" -Pattern 'OK \([0-9]+ tests?\)' -Quiet
    "FINISH $name pass=$passed $(Get-Date -Format o)" | Add-Content "$base\runtime\progress.txt"
    if(-not $passed) { throw "Runtime scenario failed: $name. See log; no PASS inferred." }
}
if(-not $SkipInstall) {
    & $adb -s $device install -r "$PSScriptRoot\..\app\build\outputs\apk\debug\app-debug.apk"
    if($LASTEXITCODE -ne 0) { throw 'QA APK installation failed' }
    & $adb -s $device install -r "$PSScriptRoot\..\app\build\outputs\apk\androidTest\debug\app-debug-androidTest.apk"
    if($LASTEXITCODE -ne 0) { throw 'Instrumentation APK installation failed' }
}
& $adb -s $device shell settings put global window_animation_scale 0
& $adb -s $device shell settings put global transition_animation_scale 0
& $adb -s $device shell settings put global animator_duration_scale 0
& $adb -s $device shell settings put secure show_ime_with_hard_keyboard 1
& $adb -s $device shell wm size 786x1704
& $adb -s $device shell wm density 320
& $adb -s $device shell input keyevent 82
if(-not $MatrixOnly -and -not $MediaOnly) {
    Run-Audit 'p0-final' 'com.example.mol.ui.VideoAuditRegressionTest'
    Run-Audit 'core-community-final' 'com.example.mol.ui.ProductionStartupTest,com.example.mol.ui.ReleaseCorePersistenceTest,com.example.mol.ui.ReleaseContentAndPlayerTest,com.example.mol.ui.ReleaseCommunityPersistenceTest,com.example.mol.ui.LearningInteractionTest,com.example.mol.ui.OfferDraftRestorationTest,com.example.mol.ui.OfferAlbumStorageTest,com.example.mol.ui.AuditCommunityCreateJourneyTest'
    Run-Audit 'functional-final' 'com.example.mol.ui.ReleaseFunctionalJourneyTest#localListingEditCancelStatusAndConfirmedDeleteUseActualRepository,com.example.mol.ui.ReleaseFunctionalJourneyTest#quranReaderZoomCollapseAndReciterSelectionPersistWhileRecordingsAreDisabled,com.example.mol.ui.ReleaseFunctionalJourneyTest#globalBrowseDuasOpensVerifiedLibraryInsteadOfObsoleteSampleId'
    Run-Audit 'native-and-mushaf' 'com.example.mol.ui.DarkFinalNativeNavigationTest,com.example.mol.ui.QuranPageBackgroundTest'
    & $adb -s $device shell pm grant com.example.mol.referenceqa android.permission.POST_NOTIFICATIONS
    & $adb -s $device shell appops set com.example.mol.referenceqa SCHEDULE_EXACT_ALARM allow
    Run-Audit 'system-delivery' 'com.example.mol.ui.AuditSystemDeliveryTest'
    Run-Audit 'alarm-after-fix' 'com.example.mol.ui.AuditAlarmRegressionTest'
    Run-Audit 'restart-prepare' 'com.example.mol.ui.AuditProcessRestartTest#prepareSavedState'
    & $adb -s $device shell am force-stop com.example.mol.referenceqa
    Run-Audit 'restart-verify' 'com.example.mol.ui.AuditProcessRestartTest#verifySavedStateInNewProcessAndRestoreTestChanges'
    Run-Audit 'final-interactions' 'com.example.mol.ui.AuditFinalInteractionTest'
    Run-Audit 'timezone-change' 'com.example.mol.ui.AuditTimezoneChangeTest'
    & $adb -s $device install -r "$PSScriptRoot\..\feature\build\outputs\apk\androidTest\debug\feature-debug-androidTest.apk"
    if($LASTEXITCODE -ne 0) { throw 'Feature test APK installation failed' }
    & $adb -s $device shell pm grant com.example.feature.test android.permission.POST_NOTIFICATIONS
    $runner='com.example.feature.test/androidx.test.runner.AndroidJUnitRunner'
    try { Run-Audit 'feature-instrumented' '' } finally { $runner='com.example.mol.referenceqa.test/androidx.test.runner.AndroidJUnitRunner' }
}
if(-not $MediaOnly -and -not $CoreOnly) {
    $matrixRoot=Join-Path $base 'runtime\final-source-matrix'
    New-Item -ItemType Directory -Force $matrixRoot | Out-Null
    $cases=@(
        @{width=393;font='1.0';size='786x1704'}, @{width=360;font='1.0';size='720x1600'},
        @{width=360;font='1.3';size='720x1600'}, @{width=393;font='1.3';size='786x1704'},
        @{width=412;font='1.0';size='824x1830'}, @{width=412;font='1.3';size='824x1830'},
        @{width=600;font='1.0';size='1200x1920'}, @{width=600;font='1.3';size='1200x1920'})
    foreach($case in $cases) {
        & $adb -s $device shell wm size $case.size
        & $adb -s $device shell wm density 320
        Start-Sleep -Seconds 3
        Run-Audit "matrix-$($case.width)-$($case.font)" 'com.example.mol.ui.DarkFinalRouteMatrixTest' @('-e','darkWidth',"$($case.width)",'-e','darkFont',$case.font)
        & $adb -s $device pull "/sdcard/Android/data/com.example.mol.referenceqa/files/video-audit-matrix-20261015/$($case.width)-$($case.font)" $matrixRoot *> "$base\runtime\pull-matrix-$($case.width)-$($case.font).log"
    }
}
if(-not $MatrixOnly -and -not $CoreOnly) {
    & $adb -s $device shell wm size 786x1704
    $ids=(Get-Content "$base\video-ids.txt" -Raw).Trim()
    Run-Audit 'media-all-videos' 'com.example.mol.ui.ReleaseFunctionalJourneyTest#officialVideoReportsActualPlayingOrExplicitExternalFallback' @('-e','videoIds',$ids)
}
$captureRoot=Join-Path $base 'runtime\final-source-captures'
New-Item -ItemType Directory -Force $captureRoot | Out-Null
& $adb -s $device pull /sdcard/Android/data/com.example.mol.referenceqa/files/video-audit-20261015 $captureRoot *> "$base\runtime\pull-search-live.log"
& $adb -s $device pull /sdcard/Android/data/com.example.mol.referenceqa/files/release-journeys $captureRoot *> "$base\runtime\pull-journeys.log"
& $adb -s $device pull /sdcard/Android/data/com.example.mol.referenceqa/files/audit-final-interactions $captureRoot *> "$base\runtime\pull-final-interactions.log"
& $adb -s $device pull /sdcard/Android/data/com.example.mol.referenceqa/files/dark-native $captureRoot *> "$base\runtime\pull-native.log"
& $adb -s $device pull /sdcard/Android/data/com.example.mol.referenceqa/files/quran-background $captureRoot *> "$base\runtime\pull-quran-paper.log"
foreach($marker in @('audit-alarm-delivery.txt','audit-reminder-delivery.txt','audit-reminder-scheduled-delivery.txt','audit-live-results.txt','audit-process-restart.txt','audit-timezone-change.txt')) {
    & $adb -s $device pull "/sdcard/Android/data/com.example.mol.referenceqa/files/$marker" "$base\runtime\$marker" *> "$base\runtime\pull-$marker.log"
}
& $adb -s $device logcat -b crash -d *> "$base\runtime\crash-buffer.log"
& $adb -s $device logcat -d -s IhsanYouTube *> "$base\runtime\youtube-errors.log"
'Runtime audit completed' | Add-Content "$base\runtime\progress.txt"
