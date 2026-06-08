param(
    [string] $BazelExecutable = "bazel",
    [string] $EvidencePath = "",
    [string[]] $BuildTargets = @("//..."),
    [string[]] $TestTargets = @("//..."),
    [string[]] $ExcludedTargets = @()
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..")
Set-Location $root

if ([string]::IsNullOrWhiteSpace($EvidencePath)) {
    $EvidencePath = Join-Path $root "docs/evidence/full-bazel-workspace-evidence.md"
}

$commonArgs = @("--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
$queryExpression = "kind('.* rule', //...)"

$checks = @(
    @{
        Id = "full-workspace-build"
        Description = "Full workspace Bazel build"
        Executable = $BazelExecutable
        Arguments = @("--batch", "build") + $BuildTargets + $commonArgs
    },
    @{
        Id = "full-workspace-test"
        Description = "Full workspace Bazel test"
        Executable = $BazelExecutable
        Arguments = @("--batch", "test") + $TestTargets + $commonArgs
    },
    @{
        Id = "full-workspace-rule-query"
        Description = "Full workspace Bazel rule query"
        Executable = $BazelExecutable
        Arguments = @("--batch", "query", "--output=label_kind", "--curses=no", $queryExpression)
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
        [string[]] $Lines,
        [string] $CheckId
    )

    if (-not $Lines -or $Lines.Count -eq 0) {
        return @("No output captured.")
    }

    if ($CheckId -eq "full-workspace-rule-query") {
        $rules = @($Lines | Where-Object { $_ -match " rule " })
        $summary = New-Object System.Collections.Generic.List[string]
        $summary.Add("Rule targets: $($rules.Count)")
        foreach ($line in ($rules | Select-Object -First 40)) {
            $summary.Add($line)
        }
        if ($rules.Count -gt 40) {
            $summary.Add("... $($rules.Count - 40) additional rule targets omitted from summary.")
        }
        return @($summary)
    }

    $interesting = $Lines | Where-Object {
        $_ -match "^(ERROR|FAILED|FAIL|INFO: Build completed|INFO: Found|INFO: Analyzed|INFO: Elapsed time|INFO: Build Event Protocol files produced successfully)" -or
        $_ -match "Target .+ up-to-date" -or
        $_ -match "Build completed successfully" -or
        $_ -match "PASSED" -or
        $_ -match "Executed .+ out of .+ test" -or
        $_ -match "//sdk/rust:bytetrain_feed_sdk_test" -or
        $_ -match "//app:app"
    }

    if (-not $interesting) {
        $interesting = $Lines | Select-Object -Last 12
    }

    return @($interesting | Select-Object -First 40)
}

function Get-EffectiveExitCode {
    param(
        [int] $NativeExitCode,
        [string[]] $Lines,
        [string] $CheckId
    )

    $accessDeniedTextZh = -join @([char]0x62D2, [char]0x7EDD, [char]0x8BBF, [char]0x95EE)
    $cannotRunTextZh = -join @([char]0x65E0, [char]0x6CD5, [char]0x8FD0, [char]0x884C)
    $accessDenied = @($Lines | Where-Object {
        $_ -match "Access is denied" -or
        $_ -match [regex]::Escape($accessDeniedTextZh) -or
        $_ -match [regex]::Escape($cannotRunTextZh) -or
        $_ -match "not recognized" -or
        $_ -match "not found"
    })
    if ($accessDenied.Count -gt 0) {
        return 1
    }

    if ($CheckId -eq "full-workspace-rule-query") {
        $rules = @($Lines | Where-Object { $_ -match " rule " })
        if ($rules.Count -eq 0) {
            return 1
        }
    }

    return $NativeExitCode
}

$started = Get-Date
$startedText = $started.ToString("yyyy-MM-dd HH:mm:ss zzz")
$evidence = New-Object System.Collections.Generic.List[string]
$evidence.Add("# Full Bazel Workspace Evidence")
$evidence.Add("")
$evidence.Add("- Date: $startedText")
$evidence.Add("- Working directory: $root")
$evidence.Add('- Verification entry: `scripts/commands/verify-full-bazel-workspace.ps1`')
$evidence.Add("- Bazel executable: ``$BazelExecutable``")
$evidence.Add("- PowerShell: $($PSVersionTable.PSVersion)")
$evidence.Add("- OS: $([Environment]::OSVersion.VersionString)")
$evidence.Add("- Acceptance build targets: ``$($BuildTargets -join ' ')``")
$evidence.Add("- Acceptance test targets: ``$($TestTargets -join ' ')``")
if ($ExcludedTargets.Count -eq 0) {
    $evidence.Add("- Allowed exclusions: none.")
}
else {
    $evidence.Add("- Allowed exclusions: ``$($ExcludedTargets -join ' ')``")
}
$evidence.Add("- Gradle: not used for full-workspace acceptance.")
$evidence.Add("- iOS/Xcode: skipped because this repository is Android-only.")
$evidence.Add('- Access-denied handling: access-denied output is treated as failure even if the shell does not propagate a native non-zero exit code.')
$evidence.Add("")

$failures = New-Object System.Collections.Generic.List[string]

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
    if ($null -eq $exitCode) {
        $exitCode = 0
    }
    $exitCode = Get-EffectiveExitCode -NativeExitCode $exitCode -Lines $output -CheckId $check.Id
    $status = if ($exitCode -eq 0) { "PASS" } else { "FAIL" }
    $summary = Get-OutputSummary -Lines $output -CheckId $check.Id

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
        $failures.Add("$($check.Description) failed with exit code $exitCode.")
    }
}

if ($failures.Count -gt 0) {
    foreach ($failure in $failures) {
        Write-Host "ERROR: $failure"
    }
    throw "Full Bazel workspace verification failed. Evidence written to $EvidencePath."
}

Write-Host "Full Bazel workspace verification passed. Evidence written to $EvidencePath."
