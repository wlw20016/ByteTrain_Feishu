Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$screenPath = Join-Path $root "features/mail/ui/MailListScreen.kt"
$buildPath = Join-Path $root "features/mail/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $screenPath)) {
    $failures.Add("MailListScreen.kt exists")
} else {
    $screen = Get-Content -Encoding UTF8 -Raw $screenPath
    $screenChecks = @(
        @{
            Name = "Mail list screen factory exists"
            Pattern = "fun\s+createMailListScreen\s*\("
        },
        @{
            Name = "Mail list screen renders UnifiedListItem values"
            Pattern = "items\s*:\s*List<UnifiedListItem>"
        },
        @{
            Name = "Mail list uses scrollable layout"
            Pattern = "ScrollView\s*\("
        },
        @{
            Name = "Mail list renders title"
            Pattern = "item\.title"
        },
        @{
            Name = "Mail list renders subtitle"
            Pattern = "item\.subtitle"
        },
        @{
            Name = "Mail list renders timestamp"
            Pattern = "item\.timestampText"
        },
        @{
            Name = "Mail list renders badges"
            Pattern = "item\.badges"
        },
        @{
            Name = "Mail list renders mail card header"
            Pattern = "text\s*=\s*`"Mail`""
        }
    )

    foreach ($check in $screenChecks) {
        if ($screen -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $mainActivityPath)) {
    $failures.Add("MainActivity.kt exists")
} else {
    $mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
    $activityChecks = @(
        @{
            Name = "MainActivity imports MailItem"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.mail\.domain\.MailItem"
        },
        @{
            Name = "MainActivity uses repository provider"
            Pattern = "AppRepositoryProvider\s*\("
        },
        @{
            Name = "MainActivity imports mail mapper"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.mail\.mapper\.toUnifiedListItem"
        },
        @{
            Name = "MainActivity imports mail list screen"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.mail\.ui\.createMailListScreen"
        },
        @{
            Name = "MainActivity maps mails to unified UI"
            Pattern = "loadedMails\.map\s*\{\s*it\.toUnifiedListItem\s*\(\s*\)\s*\}"
        },
        @{
            Name = "MainActivity renders mail list route"
            Pattern = "AppRoutes\.MAIL_LIST[\s\S]*renderMailList"
        },
        @{
            Name = "MainActivity no longer uses mail placeholder"
            Pattern = "createMailPlaceholder"
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

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/mail BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "ui/MailListScreen\.kt") {
        $failures.Add("features/mail BUILD references MailListScreen.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MAIL-004 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MAIL-004 check passed."

