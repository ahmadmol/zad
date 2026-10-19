param(
    [string]$JavaHome = 'C:\Users\molha\.jdks\jbr-21.0.3',
    [string]$CandidateApk = ''
)
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$serial = 'ac190123'
$package = 'com.example.mol.referenceqa'
$adb = 'C:\android_sdk\platform-tools\adb.exe'
$aapt = 'C:\android_sdk\build-tools\35.0.0\aapt.exe'
$signer = 'C:\android_sdk\build-tools\35.0.0\apksigner.bat'
$rc = if ($CandidateApk) { (Resolve-Path -LiteralPath $CandidateApk).Path } else {
    Join-Path $root 'docs\release\apk\Ihsan-video-audit-20261015-rc.apk'
}
$stamp = [DateTimeOffset]::UtcNow.ToString('yyyyMMddTHHmmssZ')
$out = Join-Path $root ".artifacts\physical-device-qa\$stamp"
New-Item -ItemType Directory -Force $out | Out-Null

function Invoke-AdbRead([string[]]$Arguments) {
    $result = & $adb -s $serial @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "ADB read failed: $($Arguments -join ' ')`n$($result -join "`n")" }
    return ($result -join "`n").Trim()
}
function Get-Certificate([string]$Apk) {
    $result = & $signer verify --print-certs $Apk 2>&1
    if ($LASTEXITCODE -ne 0) { throw "APK signature verification failed: $Apk`n$($result -join "`n")" }
    $digest = [regex]::Match(($result -join "`n"), 'Signer #1 certificate SHA-256 digest: ([0-9a-fA-F]+)').Groups[1].Value
    if (-not $digest) { throw 'No verified signer digest returned.' }
    return $digest.ToLowerInvariant()
}

try {
    $devices = & $adb devices -l 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Cannot enumerate ADB devices.' }
    $devices | Set-Content (Join-Path $out 'adb-devices.txt') -Encoding utf8
    if (($devices -join "`n") -notmatch '(?m)^ac190123\s+device\b') {
        throw 'BLOCKED: physical phone ac190123 is not connected/authorized. No alternative device is allowed.'
    }
    $model = Invoke-AdbRead @('shell','getprop','ro.product.model')
    $maker = Invoke-AdbRead @('shell','getprop','ro.product.manufacturer')
    $hardware = Invoke-AdbRead @('shell','getprop','ro.hardware')
    $qemu = Invoke-AdbRead @('shell','getprop','ro.kernel.qemu')
    $bootQemu = Invoke-AdbRead @('shell','getprop','ro.boot.qemu')
    $characteristics = Invoke-AdbRead @('shell','getprop','ro.build.characteristics')
    if ($model -ne '23129RAA4G' -or $maker -ne 'Xiaomi' -or $qemu -eq '1' -or $bootQemu -eq '1' -or $hardware -match 'goldfish|ranchu|qemu' -or $characteristics -match 'emulator') {
        throw 'Physical-device identity guard failed. No test was launched.'
    }
    $env:JAVA_HOME = $JavaHome
    $badging = & $aapt dump badging $rc 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect RC manifest.' }
    $badging | Set-Content (Join-Path $out 'rc-manifest.txt') -Encoding utf8
    if (($badging -join "`n") -notmatch "package: name='$([regex]::Escape($package))'") { throw 'Unexpected RC package.' }
    $paths = Invoke-AdbRead @('shell','pm','path',$package)
    $base = ($paths -split "`n" | Where-Object { $_ -match '/base\.apk$' } | Select-Object -First 1) -replace '^package:',''
    if (-not $base) { throw 'Installed target base APK was not found; no install performed.' }
    $installed = Join-Path $out 'installed-base.apk'
    $pull = & $adb -s $serial pull $base $installed 2>&1
    if ($LASTEXITCODE -ne 0) { throw 'Could not inspect installed APK.' }
    $rcCertificate = Get-Certificate $rc
    $installedCertificate = Get-Certificate $installed
    if ($rcCertificate -ne $installedCertificate) { throw 'BLOCKED: signing mismatch. Do not uninstall or clear data.' }
    $installedBadging = & $aapt dump badging $installed 2>&1
    if ($LASTEXITCODE -ne 0 -or ($installedBadging -join "`n") -notmatch "package: name='$([regex]::Escape($package))'") { throw 'Installed APK package verification failed.' }
    $metadata = [ordered]@{
        capturedAtUtc = [DateTimeOffset]::UtcNow.ToString('o'); serial = $serial
        model = $model; manufacturer = $maker; hardware = $hardware
        kernelQemu = $qemu; bootQemu = $bootQemu; characteristics = $characteristics
        android = (Invoke-AdbRead @('shell','getprop','ro.build.version.release'))
        api = (Invoke-AdbRead @('shell','getprop','ro.build.version.sdk'))
        package = $package; candidateApk = $rc; rcSha256 = (Get-FileHash $rc -Algorithm SHA256).Hash
        installedSha256 = (Get-FileHash $installed -Algorithm SHA256).Hash
        certificateSha256 = $rcCertificate; status = 'IDENTITY_VERIFIED_ONLY'
    }
    $metadata | ConvertTo-Json | Set-Content (Join-Path $out 'identity.json') -Encoding utf8
    Invoke-AdbRead @('shell','pm','list','instrumentation') | Set-Content (Join-Path $out 'instrumentation.txt') -Encoding utf8
    Invoke-AdbRead @('shell','wm','size') | Set-Content (Join-Path $out 'display-size.txt') -Encoding utf8
    Invoke-AdbRead @('shell','wm','density') | Set-Content (Join-Path $out 'display-density.txt') -Encoding utf8
    Write-Output "Identity verified only. Evidence: $out"
    Write-Output 'No install, application launch, permission change, data reset, test or reboot was performed.'
} catch {
    $_.Exception.Message | Set-Content (Join-Path $out 'blocked.txt') -Encoding utf8
    Write-Error $_.Exception.Message
    exit 1
}
