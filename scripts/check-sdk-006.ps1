Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$messageAdapterPath = Join-Path $root "features/message/data/SdkMessageRepository.kt"
$mailAdapterPath = Join-Path $root "features/mail/data/SdkMailRepository.kt"
$messageBuildPath = Join-Path $root "features/message/BUILD.bazel"
$mailBuildPath = Join-Path $root "features/mail/BUILD.bazel"
$sdkTasksPath = Join-Path $root "openspec/changes/add-sdk-contract/tasks.md"
$evidencePath = Join-Path $root "docs/ai-context/sdk-adapter-evidence.md"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $messageAdapterPath)) {
    $failures.Add("SdkMessageRepository.kt exists")
} else {
    $source = Get-Content -Encoding UTF8 -Raw $messageAdapterPath
    $checks = @(
        @{ Name = "SdkMessageRepository implements MessageRepository"; Pattern = "class\s+SdkMessageRepository[\s\S]*:\s*MessageRepository" },
        @{ Name = "Message SDK client boundary exists"; Pattern = "interface\s+MessageSdkClient" },
        @{ Name = "Message SDK page DTO exists"; Pattern = "data\s+class\s+SdkMessagePage" },
        @{ Name = "Message SDK item DTO exists"; Pattern = "data\s+class\s+SdkMessageItem" },
        @{ Name = "Message adapter exposes loadPage"; Pattern = "override\s+suspend\s+fun\s+loadPage\s*\(\s*pageSize\s*:\s*Int\s*,\s*cursor\s*:\s*String\?\s*\)\s*:\s*MessagePage" },
        @{ Name = "Message adapter calls SDK client"; Pattern = "sdkClient\.getMessagePage\s*\(\s*pageSize\s*,\s*cursor\s*\)" },
        @{ Name = "Message adapter maps DTO to domain"; Pattern = "items\.map\s*\{\s*it\.toDomain\s*\(\s*\)\s*\}" },
        @{ Name = "Message adapter preserves fallback repository"; Pattern = "fallbackRepository\?\.loadPage\s*\(\s*pageSize\s*,\s*cursor\s*\)" }
    )
    foreach ($check in $checks) {
        if ($source -notmatch $check.Pattern) { $failures.Add($check.Name) }
    }
}

if (-not (Test-Path $mailAdapterPath)) {
    $failures.Add("SdkMailRepository.kt exists")
} else {
    $source = Get-Content -Encoding UTF8 -Raw $mailAdapterPath
    $checks = @(
        @{ Name = "SdkMailRepository implements MailRepository"; Pattern = "class\s+SdkMailRepository[\s\S]*:\s*MailRepository" },
        @{ Name = "Mail SDK client boundary exists"; Pattern = "interface\s+MailSdkClient" },
        @{ Name = "Mail SDK page DTO exists"; Pattern = "data\s+class\s+SdkMailPage" },
        @{ Name = "Mail SDK item DTO exists"; Pattern = "data\s+class\s+SdkMailItem" },
        @{ Name = "Mail adapter exposes loadPage"; Pattern = "override\s+suspend\s+fun\s+loadPage\s*\(\s*pageSize\s*:\s*Int\s*,\s*cursor\s*:\s*String\?\s*\)\s*:\s*MailPage" },
        @{ Name = "Mail adapter calls SDK client"; Pattern = "sdkClient\.getMailPage\s*\(\s*pageSize\s*,\s*cursor\s*\)" },
        @{ Name = "Mail adapter maps DTO to domain"; Pattern = "items\.map\s*\{\s*it\.toDomain\s*\(\s*\)\s*\}" },
        @{ Name = "Mail adapter preserves fallback repository"; Pattern = "fallbackRepository\?\.loadPage\s*\(\s*pageSize\s*,\s*cursor\s*\)" }
    )
    foreach ($check in $checks) {
        if ($source -notmatch $check.Pattern) { $failures.Add($check.Name) }
    }
}

if (-not (Test-Path $messageBuildPath)) {
    $failures.Add("features/message BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $messageBuildPath
    if ($build -notmatch "data/SdkMessageRepository\.kt") {
        $failures.Add("features/message BUILD references SdkMessageRepository.kt")
    }
}

if (-not (Test-Path $mailBuildPath)) {
    $failures.Add("features/mail BUILD.bazel exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $mailBuildPath
    if ($build -notmatch "data/SdkMailRepository\.kt") {
        $failures.Add("features/mail BUILD references SdkMailRepository.kt")
    }
}

if (-not (Test-Path $evidencePath)) {
    $failures.Add("SDK adapter evidence exists")
} else {
    $evidence = Get-Content -Encoding UTF8 -Raw $evidencePath
    foreach ($term in @("SdkMessageRepository", "SdkMailRepository", "fallback", "SDK-backed", "repository adapter")) {
        if ($evidence -notmatch [regex]::Escape($term)) {
            $failures.Add("SDK adapter evidence documents $term")
        }
    }
}

if (-not (Test-Path $sdkTasksPath)) {
    $failures.Add("add-sdk-contract tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $sdkTasksPath
    if ($tasks -notmatch "\[x\]\s+SDK-006") {
        $failures.Add("SDK-006 is marked complete")
    }
    if ($tasks -notmatch "check-sdk-006\.ps1") {
        $failures.Add("SDK-006 records check-sdk-006.ps1 evidence")
    }
    if ($tasks -notmatch "\[x\].*Kotlin mock repository") {
        $failures.Add("Kotlin mock repository fallback is marked complete")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "SDK-006 check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "SDK-006 check passed."
