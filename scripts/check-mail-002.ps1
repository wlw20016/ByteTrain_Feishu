Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$repositoryPath = Join-Path $root "features/mail/data/MockMailRepository.kt"
$buildPath = Join-Path $root "features/mail/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $repositoryPath)) {
    $failures.Add("MockMailRepository.kt exists")
} else {
    $repository = Get-Content -Encoding UTF8 -Raw $repositoryPath
    $checks = @(
        @{
            Name = "MockMailRepository implements MailRepository"
            Pattern = "class\s+MockMailRepository[\s\S]*:\s*MailRepository"
        },
        @{
            Name = "MockMailRepository defaults to 10000 records"
            Pattern = "DEFAULT_TOTAL_COUNT\s*=\s*10_000"
        },
        @{
            Name = "MockMailRepository lazily creates deterministic items"
            Pattern = "List\s*\(\s*totalCount\s*\)\s*\{\s*index\s*->\s*createMail\s*\(\s*index\s*\)"
        },
        @{
            Name = "MockMailRepository implements loadPage"
            Pattern = "override\s+suspend\s+fun\s+loadPage\s*\(\s*pageSize\s*:\s*Int\s*,\s*cursor\s*:\s*String\?\s*\)\s*:\s*MailPage"
        },
        @{
            Name = "MockMailRepository handles invalid page size"
            Pattern = "pageSize\s*<=\s*0"
        },
        @{
            Name = "MockMailRepository parses cursor"
            Pattern = "cursor\?\.toIntOrNull\s*\(\s*\)"
        },
        @{
            Name = "MockMailRepository clamps cursor"
            Pattern = "coerceIn\s*\(\s*0\s*,\s*totalCount\s*\)"
        },
        @{
            Name = "MockMailRepository returns next cursor"
            Pattern = "nextCursor\s*=\s*if\s*\(\s*hasMore\s*\)\s*endIndex\.toString\s*\(\s*\)\s*else\s*null"
        },
        @{
            Name = "MockMailRepository returns hasMore"
            Pattern = "hasMore\s*=\s*hasMore"
        },
        @{
            Name = "MockMailRepository creates attachments"
            Pattern = "attachmentCount\s*="
        },
        @{
            Name = "MockMailRepository creates mail types"
            Pattern = "mailType\s*="
        },
        @{
            Name = "MockMailRepository creates action text"
            Pattern = "actionText\s*="
        }
    )

    foreach ($check in $checks) {
        if ($repository -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/mail BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "data/MockMailRepository\.kt") {
        $failures.Add("features/mail BUILD references MockMailRepository.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MAIL-002 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MAIL-002 check passed."
