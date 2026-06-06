Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$screenPath = Join-Path $root "features/mail/ui/MailListScreen.kt"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $mainActivityPath)) {
    $failures.Add("MainActivity.kt exists")
} else {
    $mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
    $activityChecks = @(
        @{
            Name = "MainActivity uses repository provider"
            Pattern = "AppRepositoryProvider\s*\("
        },
        @{
            Name = "MainActivity stores loaded mails"
            Pattern = "loadedMails\s*=\s*mutableListOf<MailItem>\s*\("
        },
        @{
            Name = "MainActivity stores next mail cursor"
            Pattern = "nextMailCursor\s*:\s*String\?"
        },
        @{
            Name = "MainActivity stores mail hasMore state"
            Pattern = "hasMoreMails\s*:\s*Boolean"
        },
        @{
            Name = "MainActivity loads initial mail page"
            Pattern = "mailRepository\.loadPage\s*\(\s*MAIL_PAGE_SIZE\s*,\s*null\s*\)"
        },
        @{
            Name = "MainActivity loads next mail page with cursor"
            Pattern = "mailRepository\.loadPage\s*\(\s*MAIL_PAGE_SIZE\s*,\s*nextMailCursor\s*\)"
        },
        @{
            Name = "MainActivity appends loaded mail items"
            Pattern = "loadedMails\s*\+=\s*page\.items"
        },
        @{
            Name = "MainActivity updates next mail cursor"
            Pattern = "nextMailCursor\s*=\s*page\.nextCursor"
        },
        @{
            Name = "MainActivity updates mail hasMore"
            Pattern = "hasMoreMails\s*=\s*page\.hasMore"
        },
        @{
            Name = "MainActivity rerenders mail list after loading more"
            Pattern = "onLoadMore\s*=\s*\{\s*scrollY\s*->[\s\S]*mailListScrollY\s*=\s*scrollY[\s\S]*isLoadingMoreMails\s*=\s*true[\s\S]*loadNextMailPage\s*\(\s*\)[\s\S]*isLoadingMoreMails\s*=\s*false[\s\S]*renderMailList\s*\(\s*\)"
        },
        @{
            Name = "MainActivity labels total emails"
            Pattern = "Showing\s+\$\{items\.size\}\s+of\s+10000\s+emails"
        },
        @{
            Name = "MainActivity no longer uses preview-only seed data"
            Pattern = "createInitialMailItems"
            ShouldNotMatch = $true
        }
    )

    foreach ($check in $activityChecks) {
        $matched = $mainActivity -match $check.Pattern
        if (($check.ContainsKey("ShouldNotMatch") -and $check.ShouldNotMatch -and $matched) -or
            (-not $check.ContainsKey("ShouldNotMatch") -and -not $matched)) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $screenPath)) {
    $failures.Add("MailListScreen.kt exists")
} else {
    $screen = Get-Content -Encoding UTF8 -Raw $screenPath
    $screenChecks = @(
        @{
            Name = "Mail list accepts hasMore"
            Pattern = "hasMore\s*:\s*Boolean"
        },
        @{
            Name = "Mail list accepts loading-more state"
            Pattern = "isLoadingMore\s*:\s*Boolean"
        },
        @{
            Name = "Mail list accepts scroll-aware load more callback"
            Pattern = "onLoadMore\s*:\s*\(Int\)\s*->\s*Unit"
        },
        @{
            Name = "Mail list triggers load more from scroll"
            Pattern = "setOnScrollChangeListener[\s\S]*onLoadMore\s*\(\s*scrollY\s*\)"
        },
        @{
            Name = "Mail list renders loading more state"
            Pattern = "Loading more mail"
        },
        @{
            Name = "Mail list renders no more state"
            Pattern = "No more mail"
        }
    )

    foreach ($check in $screenChecks) {
        if ($screen -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if ($screen -match '"Load more"') {
    $failures.Add("Mail list no longer renders Load more button")
}

if ($failures.Count -gt 0) {
    Write-Host "MAIL-005 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MAIL-005 check passed."

