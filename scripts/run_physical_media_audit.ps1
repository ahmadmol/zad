param(
    [Parameter(Mandatory = $true)][string]$CandidateApk,
    [string]$JavaHome = 'C:\Users\molha\.jdks\jbr-21.0.3'
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$adb = 'C:\android_sdk\platform-tools\adb.exe'
$serial = 'ac190123'
$target = 'com.example.mol.referenceqa'
$runner = 'com.example.mol.referenceqa.test/androidx.test.runner.AndroidJUnitRunner'
# This runner never installs a debug build over the RC or modifies device settings.
# A backed-up, deliberately installed debug QA target is a separate prerequisite.
$preflight = & (Join-Path $PSScriptRoot 'physical_device_preflight.ps1') -JavaHome $JavaHome -CandidateApk $CandidateApk
if ($LASTEXITCODE -ne 0) { throw 'Physical identity preflight failed.' }
$identityLine = @($preflight | Where-Object { $_ -like 'Identity verified only. Evidence: *' })
if ($identityLine.Count -ne 1) { throw 'Missing identity evidence.' }
$identityDir = $identityLine[0].Substring('Identity verified only. Evidence: '.Length)
$identity = Get-Content (Join-Path $identityDir 'identity.json') -Raw | ConvertFrom-Json
if ($identity.rcSha256 -ne $identity.installedSha256) { throw 'Installed APK does not match the explicitly selected candidate. No test launched.' }
$instrumentation = & $adb -s $serial shell pm list instrumentation 2>&1
if ($LASTEXITCODE -ne 0 -or ($instrumentation -join "`n") -notmatch 'instrumentation:com\.example\.mol\.referenceqa\.test/androidx\.test\.runner\.AndroidJUnitRunner\s+\(target=com\.example\.mol\.referenceqa\)') {
    throw 'The correctly targeted QA instrumentation is not installed. No installation was attempted.'
}
$installed = & $adb -s $serial shell dumpsys package $target 2>&1
if ($LASTEXITCODE -ne 0 -or ($installed -join "`n") -notmatch 'pkgFlags=\[[^\]]*DEBUGGABLE') {
    throw 'Media probe needs the deliberately prepared debug QA target. The release RC is not replaced automatically.'
}
$inventory = Get-Content (Join-Path $root '.artifacts\physical-device-qa\offline\videos-not-tested.json') -Raw | ConvertFrom-Json
$ids = @($inventory.videoId)
if ($ids.Count -ne 86 -or @($ids | Select-Object -Unique).Count -ne 86 -or @($ids | Where-Object { $_ -notmatch '^[A-Za-z0-9_-]{11}$' }).Count) { throw 'Invalid 86-video inventory.' }
$out = Join-Path $root ('.artifacts\physical-device-qa\media-' + [DateTimeOffset]::UtcNow.ToString('yyyyMMddTHHmmssZ'))
New-Item -ItemType Directory -Force $out | Out-Null
$logProcess = $null
try {
    $logProcess = Start-Process -FilePath $adb -ArgumentList @('-s',$serial,'logcat','-v','epoch','IhsanPhysicalQa:I','IhsanYouTube:W','AndroidRuntime:E','*:S') -RedirectStandardOutput (Join-Path $out 'player-logcat.txt') -RedirectStandardError (Join-Path $out 'logcat-stderr.txt') -PassThru -WindowStyle Hidden
    & $adb -s $serial shell am instrument -w -r -e class com.example.mol.ui.PhysicalDeviceMediaAuditTest -e physicalDeviceSerial $serial -e videoIds ($ids -join ',') $runner *> (Join-Path $out 'instrumentation.txt')
    $instrumentExit = $LASTEXITCODE
} finally {
    if ($null -ne $logProcess -and -not $logProcess.HasExited) { Stop-Process -Id $logProcess.Id }
}
$transcript = Get-Content (Join-Path $out 'instrumentation.txt') -Raw
$paths = [regex]::Matches($transcript, 'physicalMediaOutput=(/storage/emulated/0/Android/data/com\.example\.mol\.referenceqa/files/physical-media-[0-9]+)')
if ($paths.Count -gt 0) {
    $deviceOutput = $paths[$paths.Count - 1].Groups[1].Value
    & $adb -s $serial pull $deviceOutput $out *> (Join-Path $out 'pull.txt')
    if ($LASTEXITCODE -ne 0) { throw "Could not retrieve phone evidence. Preserve device output: $deviceOutput" }
}
if ($instrumentExit -ne 0 -or $transcript -notmatch 'OK \(1 test\)') { throw "Media audit interrupted/failed. Partial evidence: $out" }
if ($paths.Count -eq 0) { throw "No device evidence path was reported. Do not count this run as complete: $out" }
$ledgers = @(Get-ChildItem $out -Filter 'video-results.jsonl' -File -Recurse)
if ($ledgers.Count -ne 1) { throw 'Expected one retrieved video ledger.' }
$rows = @(Get-Content $ledgers[0].FullName | ForEach-Object { $_ | ConvertFrom-Json })
if ($rows.Count -ne 86 -or @($rows.videoId | Select-Object -Unique).Count -ne 86 -or @(Compare-Object ($ids | Sort-Object) ($rows.videoId | Sort-Object)).Count) {
    throw 'Retrieved ledger does not cover exactly the 86 requested IDs.'
}
Write-Output "Media ledger collected: $out. JUnit success does not mean the 86 videos passed. Audio and release acceptance remain separate."
