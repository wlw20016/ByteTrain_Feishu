Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$messageRepositoryPath = Join-Path $root "features/message/data/MockMessageRepository.kt"
$mailRepositoryPath = Join-Path $root "features/mail/data/MockMailRepository.kt"
$matrixPath = Join-Path $root "docs/ai-context/repository-paging-tests.md"
$tasksPath = Join-Path $root "openspec/changes/add-ui-main-flow/tasks.md"

$failures = New-Object System.Collections.Generic.List[string]

function Test-RepositoryPagingSource {
    param(
        [string] $Name,
        [string] $Path,
        [string] $PageType
    )

    if (-not (Test-Path $Path)) {
        $failures.Add("$Name repository exists")
        return
    }

    $source = Get-Content -Encoding UTF8 -Raw $Path
    $checks = @(
        @{
            Name = "$Name implements loadPage"
            Pattern = "override\s+suspend\s+fun\s+loadPage\s*\(\s*pageSize\s*:\s*Int\s*,\s*cursor\s*:\s*String\?\s*\)\s*:\s*$PageType"
        },
        @{
            Name = "$Name covers empty result for invalid page size or empty source"
            Pattern = "if\s*\(\s*pageSize\s*<=\s*0\s*\|\|\s*totalCount\s*<=\s*0\s*\)[\s\S]*items\s*=\s*emptyList\s*\(\s*\)[\s\S]*nextCursor\s*=\s*null[\s\S]*hasMore\s*=\s*false"
        },
        @{
            Name = "$Name parses cursor as start index"
            Pattern = "cursor\?\.toIntOrNull\s*\(\s*\)\?\.coerceIn\s*\(\s*0\s*,\s*totalCount\s*\)\s*\?:\s*0"
        },
        @{
            Name = "$Name computes next page end index"
            Pattern = "endIndex\s*=\s*\(\s*startIndex\s*\+\s*pageSize\s*\)\.coerceAtMost\s*\(\s*totalCount\s*\)"
        },
        @{
            Name = "$Name slices page items"
            Pattern = "items\.subList\s*\(\s*startIndex\s*,\s*endIndex\s*\)"
        },
        @{
            Name = "$Name detects final page"
            Pattern = "hasMore\s*=\s*endIndex\s*<\s*totalCount"
        },
        @{
            Name = "$Name clears next cursor on final page"
            Pattern = "nextCursor\s*=\s*if\s*\(\s*hasMore\s*\)\s*endIndex\.toString\s*\(\s*\)\s*else\s*null"
        }
    )

    foreach ($check in $checks) {
        if ($source -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

Test-RepositoryPagingSource -Name "Message" -Path $messageRepositoryPath -PageType "MessagePage"
Test-RepositoryPagingSource -Name "Mail" -Path $mailRepositoryPath -PageType "MailPage"

if (-not (Test-Path $matrixPath)) {
    $failures.Add("Repository paging test matrix exists")
} else {
    $matrix = Get-Content -Encoding UTF8 -Raw $matrixPath
    $matrixChecks = @(
        "Message repository",
        "Mail repository",
        "First page",
        "Next page",
        "Last page",
        "Invalid cursor",
        "Empty result"
    )

    foreach ($check in $matrixChecks) {
        if ($matrix -notmatch [regex]::Escape($check)) {
            $failures.Add("Repository paging matrix documents $check")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("add-ui-main-flow tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+TEST-001") {
        $failures.Add("TEST-001 is marked complete")
    }
    if ($tasks -notmatch "check-test-001\.ps1") {
        $failures.Add("TEST-001 records check-test-001.ps1 evidence")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "TEST-001 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "TEST-001 check passed."
