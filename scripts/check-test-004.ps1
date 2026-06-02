Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$messageRepositoryPath = Join-Path $root "features/message/data/MockMessageRepository.kt"
$mailRepositoryPath = Join-Path $root "features/mail/data/MockMailRepository.kt"
$evidencePath = Join-Path $root "docs/ai-context/scroll-performance-evidence.md"
$tasksPath = Join-Path $root "openspec/changes/add-ui-main-flow/tasks.md"

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
        @{ Name = "MainActivity uses message page size 30"; Pattern = "MESSAGE_PAGE_SIZE\s*=\s*30" },
        @{ Name = "MainActivity uses mail page size 30"; Pattern = "MAIL_PAGE_SIZE\s*=\s*30" },
        @{ Name = "MainActivity labels 10000 conversations"; Pattern = "10000 mock conversations" },
        @{ Name = "MainActivity labels 10000 emails"; Pattern = "10000 mock emails" },
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
    foreach ($term in @("10000", "page size 30", "Messages", "Mail", "Load more", "manual acceptance")) {
        if ($evidence -notmatch [regex]::Escape($term)) {
            $failures.Add("Scroll evidence documents $term")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("add-ui-main-flow tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+TEST-004") {
        $failures.Add("TEST-004 is marked complete")
    }
    if ($tasks -notmatch "check-test-004\.ps1") {
        $failures.Add("TEST-004 records check-test-004.ps1 evidence")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "TEST-004 check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "TEST-004 check passed."
