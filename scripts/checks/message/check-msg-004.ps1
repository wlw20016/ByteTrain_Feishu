Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$screenPath = Join-Path $root "features/message/ui/MessageListScreen.kt"
$buildPath = Join-Path $root "features/message/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $screenPath)) {
    $failures.Add("MessageListScreen.kt exists")
} else {
    $screen = Get-Content -Encoding UTF8 -Raw $screenPath
    $screenChecks = @(
        @{
            Name = "Message list screen factory exists"
            Pattern = "fun\s+createMessageListScreen\s*\("
        },
        @{
            Name = "Message list screen renders UnifiedListItem values"
            Pattern = "items\s*:\s*List<UnifiedListItem>"
        },
        @{
            Name = "Message list uses scrollable layout"
            Pattern = "ScrollView\s*\("
        },
        @{
            Name = "Message list renders title"
            Pattern = "item\.title"
        },
        @{
            Name = "Message list renders subtitle"
            Pattern = "item\.subtitle"
        },
        @{
            Name = "Message list renders timestamp"
            Pattern = "item\.timestampText"
        },
        @{
            Name = "Message list renders badges"
            Pattern = "item\.badges"
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
            Name = "MainActivity uses repository provider"
            Pattern = "AppRepositoryProvider\s*\("
        },
        @{
            Name = "MainActivity imports message mapper"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.message\.mapper\.toUnifiedListItem"
        },
        @{
            Name = "MainActivity imports message list screen"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.message\.ui\.createMessageListScreen"
        },
        @{
            Name = "MainActivity loads first message page with screen-sized page size"
            Pattern = "messageRepository\.loadPage\s*\(\s*messagePageSize\s*\(\s*\)\s*,\s*null\s*\)"
        },
        @{
            Name = "MainActivity maps messages to unified UI"
            Pattern = "\.map\s*\{\s*it\.toUnifiedListItem\s*\(\s*\)\s*\}"
        },
        @{
            Name = "MainActivity renders message list route"
            Pattern = "AppRoutes\.MESSAGE_LIST[\s\S]*createMessageListScreen"
        }
    )

    foreach ($check in $activityChecks) {
        if ($mainActivity -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/message BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "ui/MessageListScreen\.kt") {
        $failures.Add("features/message BUILD references MessageListScreen.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MSG-004 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MSG-004 check passed."

