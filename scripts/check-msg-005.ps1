Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
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
            Pattern = "messageRepository\.loadPage\s*\(\s*MESSAGE_PAGE_SIZE\s*,\s*nextMessageCursor\s*\)"
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
            Name = "MainActivity rerenders after loading more"
            Pattern = "onLoadMore\s*=\s*\{[\s\S]*loadNextMessagePage\s*\(\s*\)[\s\S]*renderMessageList\s*\(\s*\)"
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
            Name = "Message list accepts load more callback"
            Pattern = "onLoadMore\s*:\s*\(\)\s*->\s*Unit"
        },
        @{
            Name = "Message list renders Load more button"
            Pattern = "text\s*=\s*`"Load more`""
        },
        @{
            Name = "Message list calls load more callback"
            Pattern = "setOnClickListener\s*\{\s*onLoadMore\s*\(\s*\)\s*\}"
        },
        @{
            Name = "Message list renders no more state"
            Pattern = "No more messages"
        }
    )

    foreach ($check in $screenChecks) {
        if ($screen -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MSG-005 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MSG-005 check passed."
