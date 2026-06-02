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
            Name = "Mail detail renders metas"
            Pattern = "item\.detail\.metas\.forEach"
        },
        @{
            Name = "Mail detail provides back action"
            Pattern = "Back to mail"
        },
        @{
            Name = "Mail detail invokes onBack"
            Pattern = "setOnClickListener\s*\{\s*onBack\s*\(\s*\)\s*\}"
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
    if ($list -notmatch "onOpenDetail\s*:\s*\(UnifiedListItem\)\s*->\s*Unit") {
        $failures.Add("Mail list accepts open detail callback")
    }
    if ($list -notmatch "setOnClickListener\s*\{\s*onOpenDetail\s*\(\s*item\s*\)\s*\}") {
        $failures.Add("Mail card opens detail on click")
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
            Pattern = "onOpenDetail\s*=\s*\{\s*item\s*->[\s\S]*selectedMailItem\s*=\s*item[\s\S]*renderMailDetail\s*\(\s*item\s*\)"
        },
        @{
            Name = "MainActivity renders mail detail screen"
            Pattern = "createMailDetailScreen\s*\("
        },
        @{
            Name = "MainActivity clears mail detail selection on back"
            Pattern = "selectedMailItem\s*=\s*null[\s\S]*renderMailList\s*\(\s*\)"
        }
    )

    foreach ($check in $activityChecks) {
        if ($mainActivity -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
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
