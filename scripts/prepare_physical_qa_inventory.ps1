$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent
$out = Join-Path $root '.artifacts\physical-device-qa\offline'
New-Item -ItemType Directory -Force $out | Out-Null
$files = @()
foreach ($module in @('app','feature','designsystem')) {
    $files += Get-ChildItem (Join-Path $root "$module\src\main") -File -Recurse
    $files += Get-Item (Join-Path $root "$module\build.gradle.kts")
}
$files += Get-Item (Join-Path $root 'build.gradle.kts'), (Join-Path $root 'settings.gradle.kts'), (Join-Path $root 'gradle.properties'), (Join-Path $root 'gradle\libs.versions.toml'), (Join-Path $root 'gradle\wrapper\gradle-wrapper.properties'), (Join-Path $root 'app\proguard-rules.pro'), (Join-Path $root 'feature\proguard-rules.pro'), (Join-Path $root 'feature\consumer-rules.pro')
$snapshot = @($files | Sort-Object FullName -Unique | ForEach-Object {
    [ordered]@{ path = $_.FullName.Substring($root.Length + 1).Replace('\','/'); bytes = $_.Length; sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash }
})
$snapshot | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $out 'source-snapshot.json') -Encoding utf8
Get-FileHash (Join-Path $out 'source-snapshot.json') -Algorithm SHA256 | Select-Object Algorithm,Hash | ConvertTo-Json | Set-Content (Join-Path $out 'source-snapshot-digest.json') -Encoding utf8
$screen = Get-Content (Join-Path $root 'app\src\main\java\com\example\mol\navigation\Screen.kt') -Raw
$routes = @([regex]::Matches($screen, 'object\s+(\w+)\s*:\s*Screen\(\s*(?:route\s*=\s*)?"([^"]+)"') | ForEach-Object {
    [ordered]@{ name = $_.Groups[1].Value; route = $_.Groups[2].Value; light = 'Not Tested'; dark = 'Not Tested'; evidence = ''; reason = 'Physical phone unavailable; rendering alone cannot establish functional acceptance.' }
})
if ($routes.Count -ne 58) { throw "Route inventory changed: expected 58, found $($routes.Count). Reconcile before testing." }
$videos = @()
foreach ($entry in @(@('sanhya','SanhyaFixtureData.kt'), @('nabiihsan','NabiIhsanFixtureData.kt'), @('fahmanallah','FahmFixtureData.kt'))) {
    $source = Join-Path $root "feature\src\main\java\com\example\feature\$($entry[0])\data\datasource\$($entry[1])"
    $ids = [regex]::Matches((Get-Content $source -Raw), 'EpisodeRecord\([^\n]*?,\s*"([A-Za-z0-9_-]{11})"') | ForEach-Object { $_.Groups[1].Value } | Select-Object -Unique
    foreach ($id in $ids) {
        $videos += [ordered]@{ collection = $entry[0]; videoId = $id; url = "https://www.youtube.com/watch?v=$id"; status = 'Not Tested'; playerState = ''; errorCode = ''; progressObserved = ''; audioObserved = ''; fallbackResult = ''; evidence = ''; reason = 'Physical phone unavailable.' }
    }
}
if ($videos.Count -ne 86) { throw "Video inventory changed: expected 86, found $($videos.Count). Reconcile before testing." }
$routes | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $out 'routes-not-tested.json') -Encoding utf8
$videos | ConvertTo-Json -Depth 4 | Set-Content (Join-Path $out 'videos-not-tested.json') -Encoding utf8
@{
    capturedAtUtc = [DateTimeOffset]::UtcNow.ToString('o'); routes = $routes.Count; videos = $videos.Count
    sourceFiles = $snapshot.Count; applicationIdProperty = '-PihsanPhysicalQa=true'
    physicalAcceptance = 'NOT_EXECUTED'; snapshotPurpose = 'Offline source identity; not proof of source-to-original-RC correspondence.'
} | ConvertTo-Json | Set-Content (Join-Path $out 'inventory-summary.json') -Encoding utf8
Write-Output "Prepared $($routes.Count) route and $($videos.Count) video records; all remain Not Tested. Source files: $($snapshot.Count)."
