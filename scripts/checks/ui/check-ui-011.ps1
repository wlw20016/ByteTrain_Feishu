Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$messageListPath = Join-Path $root "features/message/ui/MessageListScreen.kt"
$mailListPath = Join-Path $root "features/mail/ui/MailListScreen.kt"

$mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
$messageList = Get-Content -Encoding UTF8 -Raw $messageListPath
$mailList = Get-Content -Encoding UTF8 -Raw $mailListPath

$failures = New-Object System.Collections.Generic.List[string]

foreach ($entry in @(
    @{ Name = "Message"; Source = $messageList },
    @{ Name = "Mail"; Source = $mailList }
)) {
    if ($entry.Source -match 'import\s+android\.widget\.Button') {
        $failures.Add("$($entry.Name) list no longer imports Button for pagination")
    }

    if ($entry.Source -match '"Load more"') {
        $failures.Add("$($entry.Name) list removes visible Load more button text")
    }

    if ($entry.Source -notmatch 'setOnScrollChangeListener') {
        $failures.Add("$($entry.Name) list uses scroll listener to trigger loading")
    }

    if ($entry.Source -notmatch 'isLoadingMore\s*:\s*Boolean') {
        $failures.Add("$($entry.Name) list accepts loading-more state")
    }

    if ($entry.Source -notmatch 'onLoadMore\s*:\s*\(Int\)\s*->\s*Unit') {
        $failures.Add("$($entry.Name) list reports current scrollY when loading more")
    }

    if ($entry.Source -notmatch 'Loading more') {
        $failures.Add("$($entry.Name) list renders loading-more footer text")
    }
}

if ($mainActivity -notmatch 'isLoadingMoreMessages\s*:\s*Boolean') {
    $failures.Add("MainActivity tracks message loading-more state")
}

if ($mainActivity -notmatch 'isLoadingMoreMails\s*:\s*Boolean') {
    $failures.Add("MainActivity tracks mail loading-more state")
}

if ($mainActivity -notmatch 'messageListScrollY\s*=\s*scrollY[\s\S]*isLoadingMoreMessages\s*=\s*true') {
    $failures.Add("Message load-more stores current scroll before rendering loading state")
}

if ($mainActivity -notmatch 'mailListScrollY\s*=\s*scrollY[\s\S]*isLoadingMoreMails\s*=\s*true') {
    $failures.Add("Mail load-more stores current scroll before rendering loading state")
}

if ($mainActivity -notmatch 'contentContainer\.post') {
    $failures.Add("MainActivity defers page append after loading state render")
}

if ($failures.Count -gt 0) {
    Write-Host "UI-011 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI-011 check passed."

