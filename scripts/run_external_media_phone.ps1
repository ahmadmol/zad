# This task's media probe: physical Xiaomi only; no clear/uninstall/settings changes.
param([string]$EvidenceName = 'phone-foreground', [ValidateRange(0,85)][int]$SmokeCount = 0, [string]$TestClass = 'com.example.mol.ui.PhysicalDeviceMediaAuditTest')
$ErrorActionPreference = 'Stop'
if ($EvidenceName -notmatch '^phone-[a-z0-9-]+$') { throw 'Invalid evidence directory name.' }
if ($TestClass -notmatch '^com\.example\.mol\.ui\.[A-Za-z0-9]+(?:#[A-Za-z0-9]+)?$') { throw 'Invalid QA test target.' }
$root = Split-Path $PSScriptRoot -Parent
$out = Join-Path $root ('docs/release/external-evidence/2026-10-17/' + $EvidenceName)
New-Item -ItemType Directory -Force $out | Out-Null
$adb = 'C:\android_sdk\platform-tools\adb.exe'
$serial = 'ac190123'
$package = 'com.example.mol.referenceqa'
$env:JAVA_HOME = 'C:\Users\molha\.jdks\jbr-21.0.3'
$signer = 'C:\android_sdk\build-tools\35.0.0\apksigner.bat'
$debugApk = Join-Path $root 'app/build/outputs/apk/debug/app-debug.apk'
$testApk = Join-Path $root 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
function Read-Adb([string[]]$Arguments) {
    $result = & $adb -s $serial @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "ADB failed: $($Arguments -join ' ')" }
    return ($result -join "`n").Trim()
}
if ((Read-Adb @('shell','getprop','ro.product.model')) -ne '23129RAA4G' -or
    (Read-Adb @('shell','getprop','ro.product.manufacturer')) -ne 'Xiaomi' -or
    (Read-Adb @('shell','getprop','ro.kernel.qemu')) -eq '1') { throw 'Physical identity guard rejected device.' }
$apkPath = (Read-Adb @('shell','pm','path',$package)) -replace '^package:', ''
# Back up APK outside the public evidence directory; never copies user data to docs.
$backupDir = Join-Path $root '.artifacts/external-resources-phone-20261017'
New-Item -ItemType Directory -Force $backupDir | Out-Null
$backup = Join-Path $backupDir 'installed-before.apk'
$currentCopy = Join-Path $backupDir 'current-before-run.apk'
& $adb -s $serial pull $apkPath $currentCopy *> (Join-Path $out 'backup-apk.log')
if ($LASTEXITCODE -ne 0) { throw 'Cannot preserve installed QA APK.' }
if (Test-Path -LiteralPath $backup) {
    if ((Get-FileHash $currentCopy).Hash -ne (Get-FileHash $backup).Hash) { throw 'Restore original QA APK before retry; saved original was not overwritten.' }
} else { Copy-Item -LiteralPath $currentCopy -Destination $backup }
$aapt = 'C:\android_sdk\build-tools\35.0.0\aapt.exe'
$manifest = & $aapt dump badging $debugApk 2>&1
if (($manifest -join "`n") -notmatch "package: name='com.example.mol.referenceqa'") { throw 'Unexpected candidate package.' }
function Signer-Digest([string]$Path) {
    $result = & $signer verify --print-certs $Path 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Signature verification failed.' }
    $match = [regex]::Match(($result -join "`n"), 'Signer #1 certificate SHA-256 digest: ([a-fA-F0-9]+)')
    if (-not $match.Success) { throw 'Missing certificate digest.' }
    return $match.Groups[1].Value.ToLowerInvariant()
}
if ((Signer-Digest $backup) -ne (Signer-Digest $debugApk)) { throw 'Signature mismatch; no install attempted.' }
$ids = @(Get-Content (Join-Path $root 'docs/release/external-evidence/2026-10-17/video-metadata.json') -Raw | ConvertFrom-Json | ForEach-Object { $_.video_id })
$collections = @(Get-Content (Join-Path $root 'docs/release/external-evidence/2026-10-17/video-metadata.json') -Raw | ConvertFrom-Json | ForEach-Object { $_.collection })
if ($ids.Count -ne 86 -or @($ids | Select-Object -Unique).Count -ne 86) { throw 'Invalid video inventory.' }
if ($SmokeCount -gt 0) { $ids = @($ids | Select-Object -First $SmokeCount); $collections = @($collections | Select-Object -First $SmokeCount) }
$identity = [ordered]@{serial=$serial;model='23129RAA4G';scope='DEBUG_QA_PLAYER_PROBE';startedAt=[DateTimeOffset]::UtcNow.ToString('o'); originalApkSha256=(Get-FileHash $backup).Hash; probeApkSha256=(Get-FileHash $debugApk).Hash; testApkSha256=(Get-FileHash $testApk).Hash}
$identity | ConvertTo-Json | Set-Content (Join-Path $out 'identity.json') -Encoding utf8
$installed = $false
$logProcess = $null
try {
    & $adb -s $serial install -r $debugApk *> (Join-Path $out 'install-debug.log')
    if ($LASTEXITCODE -ne 0) { throw 'Debug install failed; no uninstall or data clear will be attempted.' }
    $installed = $true
    & $adb -s $serial install -r -t $testApk *> (Join-Path $out 'install-test.log')
    if ($LASTEXITCODE -ne 0) { throw 'Test install failed.' }
    $logProcess = Start-Process -FilePath $adb -ArgumentList @('-s',$serial,'logcat','-T','1','-v','epoch','IhsanPhysicalQa:I','IhsanYouTube:W','AndroidRuntime:E','*:S') -WindowStyle Hidden -RedirectStandardOutput (Join-Path $out 'player-logcat.txt') -RedirectStandardError (Join-Path $out 'logcat-stderr.txt') -PassThru
    & $adb -s $serial shell am instrument -w -r -e class $TestClass -e physicalDeviceSerial $serial -e videoIds ($ids -join ',') -e videoCollections ($collections -join ',') -e smokeOnly ($SmokeCount -gt 0).ToString().ToLowerInvariant() 'com.example.mol.referenceqa.test/androidx.test.runner.AndroidJUnitRunner' *> (Join-Path $out 'instrumentation.txt')
    $testExit = $LASTEXITCODE
    $transcript = Get-Content (Join-Path $out 'instrumentation.txt') -Raw
    $paths = [regex]::Matches($transcript, 'physicalMediaOutput=(/storage/emulated/0/Android/data/com\.example\.mol\.referenceqa/files/physical-media-[0-9]+)')
    if ($paths.Count) {
        & $adb -s $serial pull $paths[$paths.Count-1].Groups[1].Value $out *> (Join-Path $out 'pull.log')
    }
    if ($testExit -ne 0 -or $transcript -notmatch 'OK \(1 test\)') { throw 'Phone probe incomplete; inspect individual evidence.' }
} finally {
    if ($logProcess -and -not $logProcess.HasExited) { Stop-Process -Id $logProcess.Id }
    if ($installed) {
        & (Join-Path $PSScriptRoot 'restore_external_phone_qa.ps1') -EvidenceName $EvidenceName
    }
}
Write-Output "Phone evidence: $out. Inspect ledger; completed JUnit does not prove all media played or audible audio."
