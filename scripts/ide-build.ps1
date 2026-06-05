param(
    [ValidateSet("app", "gradle-app", "proto", "features", "rust", "query-app-deps")]
    [string] $Target = "app"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
Set-Location $root

function Invoke-IdeCommand {
    param(
        [Parameter(Mandatory = $true)]
        [string] $Description,
        [Parameter(Mandatory = $true)]
        [string] $Executable,
        [Parameter(Mandatory = $true)]
        [string[]] $Arguments
    )

    Write-Host "IDE build helper: $Description"
    Write-Host "Working directory: $root"
    Write-Host "Command: $Executable $($Arguments -join ' ')"
    & $Executable @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

switch ($Target) {
    "app" {
        Invoke-IdeCommand `
            -Description "Bazel Android app build" `
            -Executable "bazel" `
            -Arguments @("--batch", "build", "//app:app", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    }
    "gradle-app" {
        Invoke-IdeCommand `
            -Description "Gradle Android debug build" `
            -Executable ".\gradlew.bat" `
            -Arguments @(":app:assembleDebug")
    }
    "proto" {
        Invoke-IdeCommand `
            -Description "Bazel proto build" `
            -Executable "bazel" `
            -Arguments @("build", "//proto:...", "--curses=no", "--show_progress_rate_limit=60", "--jobs=4")
    }
    "features" {
        Invoke-IdeCommand `
            -Description "Bazel shared and feature Kotlin build" `
            -Executable "bazel" `
            -Arguments @(
                "--batch",
                "build",
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
                "//features/mail:mail",
                "--curses=no",
                "--show_progress_rate_limit=60",
                "--jobs=4"
            )
    }
    "rust" {
        Invoke-IdeCommand `
            -Description "Rust SDK tests" `
            -Executable "cargo" `
            -Arguments @("test", "--manifest-path", "sdk/rust/Cargo.toml")
    }
    "query-app-deps" {
        Invoke-IdeCommand `
            -Description "Bazel app dependency query" `
            -Executable "bazel" `
            -Arguments @("--batch", "query", "--notool_deps", "--noimplicit_deps", "--output=label_kind", "--curses=no", "deps(//app:app, 2)")
    }
}
