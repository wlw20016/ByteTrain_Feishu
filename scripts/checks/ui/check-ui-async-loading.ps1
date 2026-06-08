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

if ($mainActivity -match '\brunSuspendBlocking\b') {
    $failures.Add("MainActivity must not contain runSuspendBlocking")
}

if ($mainActivity -match 'CountDownLatch|\.await\s*\(') {
    $failures.Add("MainActivity must not wait on latch-style blocking helpers")
}

foreach ($check in @(
    @{ Name = "MainActivity imports PagingUiState"; Pattern = 'import\s+com\.bytetrain\.feishuclone\.shared\.list\.PagingUiState' },
    @{ Name = "MainActivity creates a background executor"; Pattern = 'Executors\.newSingleThreadExecutor' },
    @{ Name = "MainActivity posts repository results to main thread"; Pattern = 'Handler\s*\(\s*Looper\.getMainLooper\s*\(\s*\)\s*\)[\s\S]*mainHandler\.post' },
    @{ Name = "MainActivity starts suspend work without blocking"; Pattern = 'private\s+fun\s+<T>\s+launchRepositoryLoad[\s\S]*block\.startCoroutine' },
    @{ Name = "Message first page uses async loading state"; Pattern = 'messagePagingState\s*=\s*PagingUiState\.Loading[\s\S]*launchRepositoryLoad\s*\([\s\S]*messageRepository\.loadPage\s*\(\s*messagePageSize\s*\(\s*\)\s*,\s*null\s*\)' },
    @{ Name = "Mail first page uses async loading state"; Pattern = 'mailPagingState\s*=\s*PagingUiState\.Loading[\s\S]*launchRepositoryLoad\s*\([\s\S]*mailRepository\.loadPage\s*\(\s*mailPageSize\s*\(\s*\)\s*,\s*null\s*\)' },
    @{ Name = "Message load-more has duplicate guard"; Pattern = 'if\s*\(\s*!hasMoreMessages\s*\|\|\s*isLoadingInitialMessages\s*\|\|\s*isLoadingMoreMessages\s*\)' },
    @{ Name = "Mail load-more has duplicate guard"; Pattern = 'if\s*\(\s*!hasMoreMails\s*\|\|\s*isLoadingInitialMails\s*\|\|\s*isLoadingMoreMails\s*\)' },
    @{ Name = "Message load-more renders LoadingMore"; Pattern = 'messagePagingState\s*=\s*PagingUiState\.LoadingMore\s*\(' },
    @{ Name = "Mail load-more renders LoadingMore"; Pattern = 'mailPagingState\s*=\s*PagingUiState\.LoadingMore\s*\(' },
    @{ Name = "Message load-more failure preserves items"; Pattern = 'messagePagingState\s*=\s*PagingUiState\.LoadMoreError\s*\([\s\S]*items\s*=\s*loadedMessages\.toList\s*\(\s*\)' },
    @{ Name = "Mail load-more failure preserves items"; Pattern = 'mailPagingState\s*=\s*PagingUiState\.LoadMoreError\s*\([\s\S]*items\s*=\s*loadedMails\.toList\s*\(\s*\)' },
    @{ Name = "Message list receives PagingUiState"; Pattern = 'createMessageListScreen\s*\([\s\S]*state\s*=\s*messageListUiState\s*\(\s*\)' },
    @{ Name = "Mail list receives PagingUiState"; Pattern = 'createMailListScreen\s*\([\s\S]*state\s*=\s*mailListUiState\s*\(\s*\)' }
)) {
    if ($mainActivity -notmatch $check.Pattern) {
        $failures.Add($check.Name)
    }
}

foreach ($entry in @(
    @{ Name = "Message"; Source = $messageList },
    @{ Name = "Mail"; Source = $mailList }
)) {
    if ($entry.Source -notmatch 'state\s*:\s*PagingUiState<UnifiedListItem>') {
        $failures.Add("$($entry.Name) list accepts PagingUiState")
    }

    foreach ($state in @(
        'PagingUiState\.Loading',
        'PagingUiState\.Empty',
        'PagingUiState\.Error',
        'PagingUiState\.Content',
        'PagingUiState\.LoadingMore',
        'PagingUiState\.LoadMoreError'
    )) {
        if ($entry.Source -notmatch $state) {
            $failures.Add("$($entry.Name) list renders $state")
        }
    }

    if ($entry.Source -notmatch 'val\s+contentState\s*=\s*state\s+as\?\s+PagingUiState\.Content') {
        $failures.Add("$($entry.Name) list only scroll-triggers from Content state")
    }

    if ($entry.Source -notmatch 'ViewTreeObserver\.OnPreDrawListener[\s\S]*scrollTo\s*\(\s*0,\s*initialScrollY\s*\)') {
        $failures.Add("$($entry.Name) list restores scroll before first draw")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "UI async loading check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI async loading check passed."
