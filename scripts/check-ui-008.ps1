Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$messageListPath = Join-Path $root "features/message/ui/MessageListScreen.kt"
$messageDetailPath = Join-Path $root "features/message/ui/MessageDetailScreen.kt"
$mailDetailPath = Join-Path $root "features/mail/ui/MailDetailScreen.kt"

$mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
$messageList = Get-Content -Encoding UTF8 -Raw $messageListPath
$messageDetail = Get-Content -Encoding UTF8 -Raw $messageDetailPath
$mailDetail = Get-Content -Encoding UTF8 -Raw $mailDetailPath

$failures = New-Object System.Collections.Generic.List[string]

if ($mainActivity -notmatch 'override\s+fun\s+onBackPressed\s*\(') {
    $failures.Add("Activity handles Android system back navigation")
}

if ($mainActivity -notmatch 'OnBackInvokedDispatcher') {
    $failures.Add("Activity registers Android 13+ system back callback")
}

if ($mainActivity -notmatch 'registerOnBackInvokedCallback') {
    $failures.Add("Activity wires OnBackInvokedDispatcher callback")
}

if ($mainActivity -notmatch 'handleBackNavigation') {
    $failures.Add("System back logic is shared across modern and legacy callbacks")
}

if ($mainActivity -notmatch 'override\s+fun\s+dispatchKeyEvent\s*\(') {
    $failures.Add("Activity handles emulator toolbar/key BACK events")
}

if ($mainActivity -notmatch 'KeyEvent\.KEYCODE_BACK') {
    $failures.Add("Activity checks KEYCODE_BACK fallback")
}

if ($mainActivity -notmatch 'messageListScrollY\s*:\s*Int') {
    $failures.Add("MainActivity stores message list scroll position")
}

if ($mainActivity -notmatch 'mailListScrollY\s*:\s*Int') {
    $failures.Add("MainActivity stores mail list scroll position")
}

if ($mainActivity -notmatch 'messageListScrollY\s*=\s*scrollY') {
    $failures.Add("Opening message detail records current message list scroll")
}

if ($mainActivity -notmatch 'mailListScrollY\s*=\s*scrollY') {
    $failures.Add("Opening mail detail records current mail list scroll")
}

if ($messageList -notmatch 'initialScrollY\s*:\s*Int') {
    $failures.Add("Message list accepts initial scroll position")
}

if ($messageList -notmatch 'scrollTo\s*\(\s*0,\s*initialScrollY\s*\)') {
    $failures.Add("Message list restores scroll position after render")
}

if ($messageList -notmatch 'onOpenDetail\s*:\s*\(UnifiedListItem,\s*Int\)\s*->\s*Unit') {
    $failures.Add("Message list passes current scroll position when opening detail")
}

if ($messageDetail -match 'import\s+android\.widget\.Button') {
    $failures.Add("Message detail no longer imports Button")
}

if ($messageDetail -match 'Back to messages') {
    $failures.Add("Message detail removes in-page Back to messages button")
}

if ($messageDetail -notmatch 'setOnClickListener\s*\{\s*onBack\s*\(\s*\)\s*\}') {
    $failures.Add("Message detail includes a compact header back affordance")
}

if ($messageDetail -match 'item\.detail\.metas\.forEach') {
    $failures.Add("Message detail does not expose internal meta rows")
}

if ($messageDetail -notmatch 'createOutgoingBubble') {
    $failures.Add("Message detail renders an outgoing chat bubble")
}

if ($messageDetail -notmatch 'createIncomingBubble') {
    $failures.Add("Message detail renders incoming chat bubbles")
}

if ($messageDetail -notmatch 'createComposerBar') {
    $failures.Add("Message detail renders a chat composer bar")
}

if ($mailDetail -match 'Back to mail') {
    $failures.Add("Mail detail removes in-page Back to mail button")
}

if ($failures.Count -gt 0) {
    Write-Host "UI-008 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI-008 check passed."
