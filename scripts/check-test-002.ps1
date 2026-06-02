Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$messageMapperPath = Join-Path $root "features/message/mapper/MessageUiMapper.kt"
$mailMapperPath = Join-Path $root "features/mail/mapper/MailUiMapper.kt"
$matrixPath = Join-Path $root "docs/ai-context/mapper-field-tests.md"
$tasksPath = Join-Path $root "openspec/changes/add-ui-main-flow/tasks.md"

$failures = New-Object System.Collections.Generic.List[string]

function Test-SourceChecks {
    param(
        [string] $Name,
        [string] $Path,
        [array] $Checks
    )

    if (-not (Test-Path $Path)) {
        $failures.Add("$Name mapper exists")
        return
    }

    $source = Get-Content -Encoding UTF8 -Raw $Path
    foreach ($check in $Checks) {
        if ($source -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

Test-SourceChecks -Name "Message" -Path $messageMapperPath -Checks @(
    @{
        Name = "Message mapper exposes toUnifiedListItem"
        Pattern = "fun\s+MessageItem\.toUnifiedListItem\s*\(\s*\)\s*:\s*UnifiedListItem"
    },
    @{
        Name = "Message mapper preserves id"
        Pattern = "id\s*=\s*id"
    },
    @{
        Name = "Message mapper maps conversation name to title"
        Pattern = "title\s*=\s*conversationName"
    },
    @{
        Name = "Message mapper maps last message preview to subtitle"
        Pattern = "subtitle\s*=\s*lastMessagePreview"
    },
    @{
        Name = "Message mapper formats timestamp"
        Pattern = "timestampText\s*=\s*formatMessageTimestamp\s*\(\s*lastMessageTimeMillis\s*\)"
    },
    @{
        Name = "Message mapper maps avatar label and image"
        Pattern = "AvatarModel[\s\S]*label\s*=\s*avatarText[\s\S]*imageUrl\s*=\s*avatarUrl"
    },
    @{
        Name = "Message mapper maps dense display style"
        Pattern = "displayStyle\s*=\s*DisplayStyle\.DENSE_CONVERSATION"
    },
    @{
        Name = "Message mapper maps unread badge"
        Pattern = "unreadCount\s*>\s*0[\s\S]*tone\s*=\s*`"unread`""
    },
    @{
        Name = "Message mapper maps pinned muted bot badges"
        Pattern = "isPinned[\s\S]*isMuted[\s\S]*isBot"
    },
    @{
        Name = "Message mapper maps detail title body and metas"
        Pattern = "DetailModel[\s\S]*title\s*=\s*conversationName[\s\S]*body\s*=\s*lastMessagePreview[\s\S]*DetailMeta"
    }
)

Test-SourceChecks -Name "Mail" -Path $mailMapperPath -Checks @(
    @{
        Name = "Mail mapper exposes toUnifiedListItem"
        Pattern = "fun\s+MailItem\.toUnifiedListItem\s*\(\s*\)\s*:\s*UnifiedListItem"
    },
    @{
        Name = "Mail mapper preserves id"
        Pattern = "id\s*=\s*id"
    },
    @{
        Name = "Mail mapper maps subject to title"
        Pattern = "title\s*=\s*subject"
    },
    @{
        Name = "Mail mapper maps preview to subtitle"
        Pattern = "subtitle\s*=\s*preview"
    },
    @{
        Name = "Mail mapper formats timestamp"
        Pattern = "timestampText\s*=\s*formatMailTimestamp\s*\(\s*timestampMillis\s*\)"
    },
    @{
        Name = "Mail mapper maps sender avatar"
        Pattern = "AvatarModel[\s\S]*label\s*=\s*sender\.firstLabel\s*\(\s*\)"
    },
    @{
        Name = "Mail mapper maps mail card display style"
        Pattern = "displayStyle\s*=\s*DisplayStyle\.MAIL_CARD"
    },
    @{
        Name = "Mail mapper maps unread badge"
        Pattern = "if\s*\(\s*unread\s*\)[\s\S]*tone\s*=\s*`"unread`""
    },
    @{
        Name = "Mail mapper maps attachment badge"
        Pattern = "attachmentCount\s*>\s*0[\s\S]*tone\s*=\s*`"attachment`""
    },
    @{
        Name = "Mail mapper maps type and action badges"
        Pattern = "mailType\.displayText\s*\(\s*\)[\s\S]*actionText\?\.takeIf"
    },
    @{
        Name = "Mail mapper maps detail title body and metas"
        Pattern = "DetailModel[\s\S]*title\s*=\s*subject[\s\S]*body\s*=\s*preview[\s\S]*DetailMeta"
    }
)

if (-not (Test-Path $matrixPath)) {
    $failures.Add("Mapper field test matrix exists")
} else {
    $matrix = Get-Content -Encoding UTF8 -Raw $matrixPath
    $matrixChecks = @(
        "Message mapper",
        "Mail mapper",
        "id",
        "title",
        "subtitle",
        "timestampText",
        "avatar",
        "badges",
        "displayStyle",
        "detail"
    )

    foreach ($check in $matrixChecks) {
        if ($matrix -notmatch [regex]::Escape($check)) {
            $failures.Add("Mapper matrix documents $check")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("add-ui-main-flow tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+TEST-002") {
        $failures.Add("TEST-002 is marked complete")
    }
    if ($tasks -notmatch "check-test-002\.ps1") {
        $failures.Add("TEST-002 records check-test-002.ps1 evidence")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "TEST-002 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "TEST-002 check passed."
