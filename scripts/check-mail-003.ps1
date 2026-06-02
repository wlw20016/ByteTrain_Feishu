Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mapperPath = Join-Path $root "features/mail/mapper/MailUiMapper.kt"
$mailItemPath = Join-Path $root "features/mail/domain/MailItem.kt"
$buildPath = Join-Path $root "features/mail/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $mailItemPath)) {
    $failures.Add("MailItem.kt exists")
} else {
    $mailItem = Get-Content -Encoding UTF8 -Raw $mailItemPath
    $domainChecks = @(
        @{
            Name = "MailItem exposes attachment count"
            Pattern = "val\s+attachmentCount\s*:\s*Int"
        },
        @{
            Name = "MailItem exposes mail type"
            Pattern = "val\s+mailType\s*:\s*MailType"
        },
        @{
            Name = "MailItem exposes action text"
            Pattern = "val\s+actionText\s*:\s*String\?"
        },
        @{
            Name = "MailType enum exists"
            Pattern = "enum\s+class\s+MailType"
        }
    )

    foreach ($check in $domainChecks) {
        if ($mailItem -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $mapperPath)) {
    $failures.Add("MailUiMapper.kt exists")
} else {
    $mapper = Get-Content -Encoding UTF8 -Raw $mapperPath
    $mapperChecks = @(
        @{
            Name = "MailItem extension maps to UnifiedListItem"
            Pattern = "fun\s+MailItem\.toUnifiedListItem\s*\(\s*\)\s*:\s*UnifiedListItem"
        },
        @{
            Name = "Mapper imports UnifiedListItem"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.shared\.ui\.UnifiedListItem"
        },
        @{
            Name = "Mapper uses mail id"
            Pattern = "id\s*=\s*id"
        },
        @{
            Name = "Mapper uses subject as title"
            Pattern = "title\s*=\s*subject"
        },
        @{
            Name = "Mapper uses preview as subtitle"
            Pattern = "subtitle\s*=\s*preview"
        },
        @{
            Name = "Mapper uses mail card display style"
            Pattern = "DisplayStyle\.MAIL_CARD"
        },
        @{
            Name = "Mapper carries unread badge"
            Pattern = "unread"
        },
        @{
            Name = "Mapper carries attachment badge"
            Pattern = "attachmentCount\s*>\s*0"
        },
        @{
            Name = "Mapper carries mail type"
            Pattern = "mailType\.displayText"
        },
        @{
            Name = "Mapper carries action text"
            Pattern = "actionText"
        },
        @{
            Name = "Mapper creates detail model"
            Pattern = "DetailModel\s*\("
        }
    )

    foreach ($check in $mapperChecks) {
        if ($mapper -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/mail BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "mapper/MailUiMapper\.kt") {
        $failures.Add("features/mail BUILD references MailUiMapper.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MAIL-003 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MAIL-003 check passed."
