Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$modelsPath = Join-Path $root "shared/ui/UnifiedUiModels.kt"
$buildPath = Join-Path $root "shared/ui/BUILD.bazel"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $modelsPath)) {
    $failures.Add("Unified UI model file exists")
} else {
    $source = Get-Content -Encoding UTF8 -Raw $modelsPath
    $checks = @(
        @{
            Name = "Models use shared ui package"
            Pattern = "package\s+com\.bytetrain\.feishuclone\.shared\.ui"
        },
        @{
            Name = "UnifiedListItem is declared"
            Pattern = "data\s+class\s+UnifiedListItem\s*\("
        },
        @{
            Name = "AvatarModel is declared"
            Pattern = "data\s+class\s+AvatarModel\s*\("
        },
        @{
            Name = "BadgeModel is declared"
            Pattern = "data\s+class\s+BadgeModel\s*\("
        },
        @{
            Name = "DisplayStyle is declared"
            Pattern = "enum\s+class\s+DisplayStyle"
        },
        @{
            Name = "DetailModel is declared"
            Pattern = "data\s+class\s+DetailModel\s*\("
        },
        @{
            Name = "DetailMeta is declared"
            Pattern = "data\s+class\s+DetailMeta\s*\("
        },
        @{
            Name = "UnifiedListItem exposes detail model"
            Pattern = "detail\s*:\s*DetailModel"
        },
        @{
            Name = "Models do not depend on feature modules"
            Pattern = "features\.(message|mail)"
            ShouldNotMatch = $true
        },
        @{
            Name = "Models do not depend on Android framework"
            Pattern = "import\s+android\."
            ShouldNotMatch = $true
        }
    )

    foreach ($check in $checks) {
        $matched = $source -match $check.Pattern
        $shouldNotMatch = $check.ContainsKey("ShouldNotMatch") -and $check.ShouldNotMatch
        if (($shouldNotMatch -and $matched) -or (-not $shouldNotMatch -and -not $matched)) {
            $failures.Add($check.Name)
        }
    }
}

if (-not (Test-Path $buildPath)) {
    $failures.Add("shared/ui BUILD file exists")
} else {
    $build = Get-Content -Encoding UTF8 -Raw $buildPath
    if ($build -notmatch "UnifiedUiModels\.kt") {
        $failures.Add("shared/ui BUILD references UnifiedUiModels.kt")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "UI-003 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI-003 check passed."
