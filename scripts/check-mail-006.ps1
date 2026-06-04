Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$listPath = Join-Path $root "features/mail/ui/MailListScreen.kt"
$detailPath = Join-Path $root "features/mail/ui/MailDetailScreen.kt"
$buildPath = Join-Path $root "features/mail/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $detailPath)) {
    $failures.Add("MailDetailScreen.kt exists")
} else {
    $detail = Get-Content -Encoding UTF8 -Raw $detailPath
    $detailChecks = @(
        @{
            Name = "Mail detail screen factory exists"
            Pattern = "fun\s+createMailDetailScreen\s*\("
        },
        @{
            Name = "Mail detail renders detail title"
            Pattern = "item\.detail\.title"
        },
        @{
            Name = "Mail detail renders detail body"
            Pattern = "item\.detail\.body"
        },
        @{
            Name = "Mail detail renders compact header back affordance"
            Pattern = "setOnClickListener\s*\{\s*onBack\s*\(\s*\)\s*\}"
        },
        @{
            Name = "Mail detail renders sender row"
            Pattern = "createSenderRow"
        },
        @{
            Name = "Mail detail renders useful badges"
            Pattern = "createBadgeRow"
        }
    )

    foreach ($check in $detailChecks) {
        if ($detail -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $listPath)) {
    $failures.Add("MailListScreen.kt exists")
} else {
    $list = Get-Content -Encoding UTF8 -Raw $listPath
    if ($list -notmatch "onOpenDetail\s*:\s*\(UnifiedListItem,\s*Int\)\s*->\s*Unit") {
        $failures.Add("Mail list accepts open detail callback with scroll position")
    }
    if ($list -notmatch "onOpenDetail\s*\(\s*item,\s*scrollY\s*\)") {
        $failures.Add("Mail card opens detail with current scroll position")
    }
}

if (-not (Test-Path $mainActivityPath)) {
    $failures.Add("MainActivity.kt exists")
} else {
    $mainActivity = Get-Content -Encoding UTF8 -Raw $mainActivityPath
    $activityChecks = @(
        @{
            Name = "MainActivity imports mail detail screen"
            Pattern = "import\s+com\.bytetrain\.feishuclone\.features\.mail\.ui\.createMailDetailScreen"
        },
        @{
            Name = "MainActivity tracks selected mail"
            Pattern = "selectedMailItem\s*:\s*UnifiedListItem\?"
        },
        @{
            Name = "MainActivity opens mail detail"
            Pattern = "onOpenDetail\s*=\s*\{\s*item,\s*scrollY\s*->[\s\S]*mailListScrollY\s*=\s*scrollY[\s\S]*selectedMailItem\s*=\s*item[\s\S]*renderMailDetail\s*\(\s*item\s*\)"
        },
        @{
            Name = "MainActivity renders mail detail screen"
            Pattern = "createMailDetailScreen\s*\("
        },
        @{
            Name = "MainActivity clears mail detail selection on system back"
            Pattern = "override\s+fun\s+onBackPressed\s*\(\s*\)[\s\S]*selectedMailItem\s*=\s*null[\s\S]*renderMailList\s*\(\s*\)"
        }
    )

    foreach ($check in $activityChecks) {
        if ($mainActivity -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if ($detail -match "Back to mail") {
    $failures.Add("Mail detail does not render an in-page Back to mail button")
}

if ($detail -match "item\.detail\.metas\.forEach") {
    $failures.Add("Mail detail does not expose internal meta rows")
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("features/mail BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "ui/MailDetailScreen\.kt") {
        $failures.Add("features/mail BUILD references MailDetailScreen.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "MAIL-006 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "MAIL-006 check passed."
