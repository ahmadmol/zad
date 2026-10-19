# Retrieve this audit's phone evidence and restore its saved QA APK. Never clears data.
param([string]$EvidenceName = 'phone-foreground')
$ErrorActionPreference = 'Stop'
if ($EvidenceName -notmatch '^phone-[a-z0-9-]+$') { throw 'Invalid evidence directory name.' }
$root = Split-Path $PSScriptRoot -Parent
$adb = 'C:\android_sdk\platform-tools\adb.exe'
$serial = 'ac190123'
$package = 'com.example.mol.referenceqa'
$out = Join-Path $root ('docs/release/external-evidence/2026-10-17/' + $EvidenceName)
$backup = Join-Path $root '.artifacts/external-resources-phone-20261017/installed-before.apk'
if ((& $adb -s $serial shell getprop ro.product.model).Trim() -ne '23129RAA4G') { throw 'Reconnect physical Xiaomi ac190123; no alternative device allowed.' }
if ((& $adb -s $serial shell getprop ro.kernel.qemu).Trim() -eq '1') { throw 'Emulator rejected.' }
if ((Get-FileHash -LiteralPath $backup).Hash -ne '514B7D101750F791BB9583236C539EE9401DB19A06C476BE545223381B30D121') { throw 'Unexpected saved APK; no installation attempted.' }
$transcript = Get-Content (Join-Path $out 'instrumentation.txt') -Raw
$paths = [regex]::Matches($transcript, 'physicalMediaOutput=(/storage/emulated/0/Android/data/com\.example\.mol\.referenceqa/files/physical-media-[0-9]+)')
try {
    if ($paths.Count) {
        & $adb -s $serial pull $paths[$paths.Count-1].Groups[1].Value $out *> (Join-Path $out 'reconnect-pull.log')
        if ($LASTEXITCODE -ne 0) { Write-Warning 'Partial media evidence could not be retrieved; restore is still required.' }
    }
} finally {
    & $adb -s $serial install -r $backup *> (Join-Path $out 'restore-after-reconnect.log')
    if ($LASTEXITCODE -ne 0) { throw 'Restore failed; saved APK retained. Never uninstall or clear data.' }
    $pathsNow = & $adb -s $serial shell pm path $package
    $base = ($pathsNow -join '').Trim() -replace '^package:', ''
    $restored = Join-Path $root '.artifacts/external-resources-phone-20261017/restored.apk'
    & $adb -s $serial pull $base $restored *> (Join-Path $out 'restore-verify.log')
    if ($LASTEXITCODE -ne 0 -or (Get-FileHash $restored).Hash -ne (Get-FileHash $backup).Hash) { throw 'Restored APK not yet byte-verified.' }
    @{status='RESTORED_BYTE_IDENTICAL';sha256=(Get-FileHash $restored).Hash;verifiedAtUtc=[DateTimeOffset]::UtcNow.ToString('o')} | ConvertTo-Json | Set-Content (Join-Path $out 'restoration.json') -Encoding utf8
}
Write-Output 'Original QA APK restored and verified byte-for-byte, with no data clear or uninstall.'
