Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$screenPath = Join-Path $root "features/message/ui/MessageListScreen.kt"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $mainActivityPath)) {
    $failures.Add("MainActivity.kt exists")
} else {
    $mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
    $activityChecks = @(
        @{
            Name = "MainActivity stores loaded messages"
            Pattern = "loadedMessages\s*=\s*mutableListOf<MessageItem>\s*\("
        },
        @{
            Name = "MainActivity stores next message cursor"
            Pattern = "nextMessageCursor\s*:\s*String\?"
        },
        @{
            Name = "MainActivity stores hasMore state"
            Pattern = "hasMoreMessages\s*:\s*Boolean"
        },
        @{
            Name = "MainActivity loads next page with cursor"
            Pattern = "messageRepository\.loadPage\s*\(\s*messagePageSize\s*\(\s*\)\s*,\s*nextMessageCursor\s*\)"
        },
        @{
            Name = "MainActivity appends loaded items"
            Pattern = "loadedMessages\s*\+=\s*page\.items"
        },
        @{
            Name = "MainActivity updates next cursor"
            Pattern = "nextMessageCursor\s*=\s*page\.nextCursor"
        },
        @{
            Name = "MainActivity updates hasMore"
            Pattern = "hasMoreMessages\s*=\s*page\.hasMore"
        },
        @{
            Name = "MainActivity rerenders message list once after appending next page"
            Pattern = "onLoadMore\s*=\s*\{\s*scrollY\s*->[\s\S]*messageListScrollY\s*=\s*scrollY[\s\S]*isLoadingMoreMessages\s*=\s*true[\s\S]*loadNextMessagePage\s*\(\s*\)[\s\S]*isLoadingMoreMessages\s*=\s*false[\s\S]*renderMessageList\s*\(\s*\)"
        }
    )

    foreach ($check in $activityChecks) {
        if ($mainActivity -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $screenPath)) {
    $failures.Add("MessageListScreen.kt exists")
} else {
    $screen = Get-Content -Encoding UTF8 -Raw $screenPath
    $screenChecks = @(
        @{
            Name = "Message list accepts hasMore"
            Pattern = "hasMore\s*:\s*Boolean"
        },
        @{
            Name = "Message list accepts loading-more state"
            Pattern = "isLoadingMore\s*:\s*Boolean"
        },
        @{
            Name = "Message list accepts scroll-aware load more callback"
            Pattern = "onLoadMore\s*:\s*\(Int\)\s*->\s*Unit"
        },
        @{
            Name = "Message list triggers load more from scroll"
            Pattern = "setOnScrollChangeListener[\s\S]*onLoadMore\s*\(\s*scrollY\s*\)"
        },
        @{
            Name = "Message list renders loading more state"
            Pattern = "Loading more messages"
        },
        @{
            Name = "Message list renders no more state"
            Pattern = "No more messages"
        },
        @{
            Name = "Message list restores scroll before first draw"
            Pattern = "ViewTreeObserver\.OnPreDrawListener[\s\S]*scrollTo\s*\(\s*0,\s*initialScrollY\s*\)"
        }
    )

    foreach ($check in $screenChecks) {
        if ($screen -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if ($screen -match '"Load more"') {
    $failures.Add("Message list no longer renders Load more button")
}

if ($mainActivity -notmatch "private\s+fun\s+messagePageSize\s*\(\s*\)\s*:\s*Int\s*=[\s\S]*screenVisiblePageSize") {
    $failures.Add("Message page size is calculated from visible screen capacity")
}

if ($mainActivity -match "MESSAGE_PAGE_SIZE\s*=\s*30") {
    $failures.Add("Message page size is no longer fixed at 30")
}

if ($mainActivity -match "isLoadingMoreMessages\s*=\s*true\s*[\r\n\s]*renderMessageList\s*\(\s*\)\s*[\r\n\s]*contentContainer\.post") {
    $failures.Add("Message load-more does not rerender a top-positioned list before appending")
}

if ($screen -match "scrollView\.post\s*\{[\s\S]*scrollTo\s*\(\s*0,\s*initialScrollY\s*\)") {
    $failures.Add("Message list does not restore scroll with post after first draw")
}

if ($failures.Count -gt 0) {
    Write-Host "MSG-005 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MSG-005 check passed."

