param(
    [string] $BazelExecutable = "bazel",
    [string] $EvidencePath = ""
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $root

if ([string]::IsNullOrWhiteSpace($EvidencePath)) {
    $EvidencePath = Join-Path $root "docs/evidence/final-bazel-delivery-evidence.md"
}

$featureTargets = @(
    "//shared/list:list",
    "//shared/navigation:navigation",
    "//shared/ui:ui_models",
    "//features/message:domain",
    "//features/message:data",
    "//features/message:mapper",
    "//features/message:ui",
    "//features/message:message",
    "//features/mail:domain",
    "//features/mail:data",
    "//features/mail:mapper",
    "//features/mail:ui",
    "//features/mail:mail"
)

$checks = @(
    @{
        Id = "app-build"
        Description = "Android app Bazel build"
        Executable = $BazelExecutable
        Arguments = @("--batch", "build", "//app:app", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    },
    @{
        Id = "proto-build"
        Description = "Proto Bazel build"
        Executable = $BazelExecutable
        Arguments = @("build", "//proto:...", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    },
    @{
        Id = "shared-feature-build"
        Description = "Shared and feature Kotlin Bazel build"
        Executable = $BazelExecutable
        Arguments = @("--batch", "build") + $featureTargets + @("--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    },
    @{
        Id = "rust-sdk-test"
        Description = "Rust SDK Bazel test"
        Executable = $BazelExecutable
        Arguments = @("--batch", "test", "//sdk/rust:bytetrain_feed_sdk_test", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    },
    @{
        Id = "app-deps-query"
        Description = "App dependency query"
        Executable = $BazelExecutable
        Arguments = @("--batch", "query", "--notool_deps", "--noimplicit_deps", "--output=label_kind", "--curses=no", "deps(//app:app, 2)")
    }
)

function Format-Command {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Executable,
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments
    )

    $parts = @($Executable) + ($Arguments | ForEach-Object {
        if ($_ -match "\s") {
            '"' + ($_ -replace '"', '\"') + '"'
        }
        else {
            $_
        }
    })
    return $parts -join " "
}

function Get-OutputSummary {
    param(
        [string[]] $Lines
    )

    if (-not $Lines -or $Lines.Count -eq 0) {
        return @("No output captured.")
    }

    $interesting = $Lines | Where-Object {
        $_ -match "^(ERROR|FAILED|FAIL|INFO: Build completed|INFO: Found|INFO: Elapsed time|INFO: Build Event Protocol files produced successfully)" -or
        $_ -match "Target .+ up-to-date" -or
        $_ -match "Build completed successfully" -or
        $_ -match "PASSED" -or
        $_ -match "android_binary rule //app:app" -or
        $_ -match "kt_android_library rule //app:app_lib" -or
        $_ -match "//features/(message|mail):" -or
        $_ -match "//shared/(navigation|ui|list):"
    }

    if (-not $interesting) {
        $interesting = $Lines | Select-Object -Last 8
    }

    return @($interesting | Select-Object -First 30)
}

$started = Get-Date
$startedText = $started.ToString("yyyy-MM-dd HH:mm:ss zzz")
$evidence = New-Object System.Collections.Generic.List[string]
$evidence.Add("# Final Bazel Delivery Evidence")
$evidence.Add("")
$evidence.Add("- Date: $startedText")
$evidence.Add("- Working directory: $root")
$evidence.Add('- Verification entry: `scripts/commands/verify-final-bazel-delivery.ps1`')
$evidence.Add("- Gradle: not used for final acceptance.")
$evidence.Add("- iOS/Xcode: skipped because this repository is Android-only.")
$evidence.Add("")

foreach ($check in $checks) {
    $command = Format-Command -Executable $check.Executable -Arguments $check.Arguments

    Write-Host "Running $($check.Id): $command"
    $previousErrorActionPreference = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    try {
        $output = @(& $check.Executable @($check.Arguments) 2>&1 | ForEach-Object { $_.ToString() })
    }
    finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $exitCode = $LASTEXITCODE
    $status = if ($exitCode -eq 0) { "PASS" } else { "FAIL" }
    $summary = Get-OutputSummary -Lines $output

    $evidence.Add("## $($check.Description)")
    $evidence.Add("")
    $evidence.Add("- Status: $status")
    $evidence.Add("- Exit code: $exitCode")
    $evidence.Add("- Command: ``" + $command + "``")
    $evidence.Add("")
    $evidence.Add("Output summary:")
    $evidence.Add("")
    $evidence.Add('```text')
    foreach ($line in $summary) {
        $evidence.Add($line)
    }
    $evidence.Add('```')
    $evidence.Add("")

    $evidence | Set-Content -Path $EvidencePath -Encoding UTF8

    if ($exitCode -ne 0) {
        throw "$($check.Description) failed with exit code $exitCode. Evidence written to $EvidencePath."
    }
}

Write-Host "Final Bazel delivery verification passed. Evidence written to $EvidencePath."

