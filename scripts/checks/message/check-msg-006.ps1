Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$listPath = Join-Path $root "features/message/ui/MessageListScreen.kt"
$detailPath = Join-Path $root "features/message/ui/MessageDetailScreen.kt"
$buildPath = Join-Path $root "features/message/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $detailPath)) {
    $failures.Add("MessageDetailScreen.kt exists")
} else {
    $detail = Get-Content -Encoding UTF8 -Raw $detailPath
    $detailChecks = @(
        @{
            Name = "Message detail screen factory exists"
            Pattern = "fun\s+createMessageDetailScreen\s*\("
        },
        @{
            Name = "Message detail renders conversation title"
            Pattern = "item\.title"
        },
        @{
            Name = "Message detail renders chat body"
            Pattern = "item\.detail\.body"
        },
        @{
            Name = "Message detail renders outgoing chat bubble"
            Pattern = "createOutgoingBubble"
        },
        @{
            Name = "Message detail renders incoming chat bubbles"
            Pattern = "createIncomingBubble"
        },
        @{
            Name = "Message detail renders composer bar"
            Pattern = "createComposerBar"
        }
    )

    foreach ($check in $detailChecks) {
        if ($detail -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $listPath)) {
    $failures.Add("MessageListScreen.kt exists")
} else {
    $list = Get-Content -Encoding UTF8 -Raw $listPath
    if ($list -notmatch "onOpenDetail\s*:\s*\(UnifiedListItem,\s*Int\)\s*->\s*Unit") {
        $failures.Add("Message list accepts open detail callback with scroll position")
    }
    if ($list -notmatch "onOpenDetail\s*\(\s*item,\s*scrollY\s*\)") {
        $failures.Add("Message row opens detail with current scroll position")
    }
}

if (-not (Test-Path $mainActivityPath)) {
    $failures.Add("MainActivity.kt exists")
} else {
    $mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
    $activityChecks = @(
        @{
            Name = "MainActivity imports detail screen"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.message\.ui\.createMessageDetailScreen"
        },
        @{
            Name = "MainActivity tracks selected message"
            Pattern = "selectedMessageItem\s*:\s*UnifiedListItem\?"
        },
        @{
            Name = "MainActivity opens message detail"
            Pattern = "onOpenDetail\s*=\s*\{\s*item,\s*scrollY\s*->[\s\S]*messageListScrollY\s*=\s*scrollY[\s\S]*selectedMessageItem\s*=\s*item[\s\S]*renderMessageDetail\s*\(\s*item\s*\)"
        },
        @{
            Name = "MainActivity renders detail screen"
            Pattern = "createMessageDetailScreen\s*\("
        },
        @{
            Name = "MainActivity clears detail selection on system back"
            Pattern = "override\s+fun\s+onBackPressed\s*\(\s*\)[\s\S]*selectedMessageItem\s*=\s*null[\s\S]*renderMessageList\s*\(\s*\)"
        }
    )

    foreach ($check in $activityChecks) {
        if ($mainActivity -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if ($detail -match "Back to messages") {
    $failures.Add("Message detail does not render an in-page Back to messages button")
}

if ($detail -match "item\.detail\.metas\.forEach") {
    $failures.Add("Message detail does not expose internal meta rows")
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/message BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "ui/MessageDetailScreen\.kt") {
        $failures.Add("features/message BUILD references MessageDetailScreen.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MSG-006 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MSG-006 check passed."

