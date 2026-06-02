Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mapperPath = Join-Path $root "features/message/mapper/MessageUiMapper.kt"
$buildPath = Join-Path $root "features/message/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $mapperPath)) {
    $failures.Add("MessageUiMapper.kt exists")
} else {
    $mapper = Get-Content -Encoding UTF8 -Raw $mapperPath
    $checks = @(
        @{
            Name = "MessageItem extension maps to UnifiedListItem"
            Pattern = "fun\s+MessageItem\.toUnifiedListItem\s*\(\s*\)\s*:\s*UnifiedListItem"
        },
        @{
            Name = "Mapper imports UnifiedListItem"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.shared\.ui\.UnifiedListItem"
        },
        @{
            Name = "Mapper uses message id"
            Pattern = "id\s*=\s*id"
        },
        @{
            Name = "Mapper uses conversation name as title"
            Pattern = "title\s*=\s*conversationName"
        },
        @{
            Name = "Mapper uses preview as subtitle"
            Pattern = "subtitle\s*=\s*lastMessagePreview"
        },
        @{
            Name = "Mapper uses dense conversation display style"
            Pattern = "DisplayStyle\.DENSE_CONVERSATION"
        },
        @{
            Name = "Mapper carries unread badge"
            Pattern = "unreadCount\s*>\s*0"
        },
        @{
            Name = "Mapper carries pinned badge"
            Pattern = "isPinned"
        },
        @{
            Name = "Mapper carries muted badge"
            Pattern = "isMuted"
        },
        @{
            Name = "Mapper carries bot badge"
            Pattern = "isBot"
        },
        @{
            Name = "Mapper creates detail model"
            Pattern = "DetailModel\s*\("
        }
    )

    foreach ($check in $checks) {
        if ($mapper -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/message BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "mapper/MessageUiMapper\.kt") {
        $failures.Add("features/message BUILD references MessageUiMapper.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MSG-003 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MSG-003 check passed."
