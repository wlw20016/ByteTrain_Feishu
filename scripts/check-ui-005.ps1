Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$mockMessagesPath = Join-Path $root "features/message/data/MockMessageRepository.kt"
$messageIconPath = Join-Path $root "app/src/main/res/drawable/ic_messages_24.xml"
$mailIconPath = Join-Path $root "app/src/main/res/drawable/ic_mail_24.xml"

$mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
$mockMessages = Get-Content -Encoding UTF8 -Raw $mockMessagesPath

$failures = New-Object System.Collections.Generic.List[string]

if ($mainActivity -notmatch 'R\.drawable\.ic_messages_24') {
    $failures.Add("Message tab uses the messages icon resource")
}

if ($mainActivity -notmatch 'R\.drawable\.ic_mail_24') {
    $failures.Add("Mail tab uses the mail icon resource")
}

if ($mainActivity -notmatch 'setCompoundDrawablesWithIntrinsicBounds') {
    $failures.Add("Bottom tab buttons render compound icon drawables")
}

if ($mainActivity -notmatch 'contentDescription\s*=\s*label') {
    $failures.Add("Icon-only bottom tabs keep accessible labels")
}

if ($mainActivity -match 'createTabButton\("Messages",\s*AppRoutes\.MESSAGE_LIST\)') {
    $failures.Add("Message tab is no longer a text-first button")
}

if ($mainActivity -match 'createTabButton\("Mail",\s*AppRoutes\.MAIL_LIST\)') {
    $failures.Add("Mail tab is no longer a text-first button")
}

if ($mockMessages -match 'ConversationType\.SINGLE\s*->\s*"\$\{singleNames\[index\s*%\s*singleNames\.size\]\}\s+\$\{index\s*\+\s*1\}"') {
    $failures.Add("Single conversation names no longer append visible numeric suffixes")
}

if ($mockMessages -match 'ConversationType\.GROUP\s*->\s*"\$\{groupNames\[index\s*%\s*groupNames\.size\]\}\s+\$\{index\s*\+\s*1\}"') {
    $failures.Add("Group conversation names no longer append visible numeric suffixes")
}

if ($mockMessages -match 'ConversationType\.BOT\s*->\s*"\$\{botNames\[index\s*%\s*botNames\.size\]\}\s+\$\{index\s*\+\s*1\}"') {
    $failures.Add("Bot conversation names no longer append visible numeric suffixes")
}

if (-not (Test-Path $messageIconPath)) {
    $failures.Add("Messages vector icon exists")
}

if (-not (Test-Path $mailIconPath)) {
    $failures.Add("Mail vector icon exists")
}

if ($failures.Count -gt 0) {
    Write-Host "UI-005 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI-005 check passed."
