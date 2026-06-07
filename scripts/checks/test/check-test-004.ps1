Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$messageRepositoryPath = Join-Path $root "features/message/data/MockMessageRepository.kt"
$mailRepositoryPath = Join-Path $root "features/mail/data/MockMailRepository.kt"
$evidencePath = Join-Path $root "docs/ai-context/ui/scroll-performance-evidence.md"
$tasksPath = Join-Path $root "openspec/changes/fix-load-more-scroll-jump/tasks.md"

$failures = New-Object System.Collections.Generic.List[string]

foreach ($path in @($messageRepositoryPath, $mailRepositoryPath)) {
    if (-not (Test-Path $path)) {
        $failures.Add("$path exists")
        continue
    }
    $source = Get-Content -Encoding UTF8 -Raw $path
    if ($source -notmatch "DEFAULT_TOTAL_COUNT\s*=\s*10_000") {
        $failures.Add("$path defaults to 10000 records")
    }
}

if (-not (Test-Path $mainActivityPath)) {
    $failures.Add("MainActivity.kt exists")
} else {
    $source = Get-Content -Encoding UTF8 -Raw $mainActivityPath
    foreach ($check in @(
        @{ Name = "MainActivity calculates message page size from visible screen capacity"; Pattern = "messagePageSize\s*\(\s*\)\s*:\s*Int\s*=[\s\S]*screenVisiblePageSize" },
        @{ Name = "MainActivity calculates mail page size from visible screen capacity"; Pattern = "mailPageSize\s*\(\s*\)\s*:\s*Int\s*=[\s\S]*screenVisiblePageSize" },
        @{ Name = "MainActivity clamps dynamic page size"; Pattern = "coerceIn\s*\([\s\S]*MIN_VISIBLE_PAGE_SIZE[\s\S]*MAX_REPOSITORY_PAGE_SIZE[\s\S]*\)" },
        @{ Name = "MainActivity labels 10000 conversations"; Pattern = "10000 conversations" },
        @{ Name = "MainActivity labels 10000 emails"; Pattern = "10000 emails" },
        @{ Name = "MainActivity appends message pages"; Pattern = "loadedMessages\s*\+=\s*page\.items" },
        @{ Name = "MainActivity appends mail pages"; Pattern = "loadedMails\s*\+=\s*page\.items" }
    )) {
        if ($source -notmatch $check.Pattern) { $failures.Add($check.Name) }
    }
}

if (-not (Test-Path $evidencePath)) {
    $failures.Add("Scroll performance evidence exists")
} else {
    $evidence = Get-Content -Encoding UTF8 -Raw $evidencePath
    foreach ($term in @("10000", "screen-sized page", "Messages", "Mail", "Pull up to load more", "manual acceptance")) {
        if ($evidence -notmatch [regex]::Escape($term)) {
            $failures.Add("Scroll evidence documents $term")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("fix-load-more-scroll-jump tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+UI-LOAD-006") {
        $failures.Add("UI-LOAD-006 is marked complete")
    }
    if ($tasks -notmatch "check-test-004\.ps1") {
        $failures.Add("UI-LOAD-006 records check-test-004.ps1 evidence")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "TEST-004 check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "TEST-004 check passed."

