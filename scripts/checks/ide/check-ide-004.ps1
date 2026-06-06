Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$paths = @{
    Extensions = Join-Path $root ".vscode/extensions.json"
    Settings = Join-Path $root ".vscode/settings.json"
    Launch = Join-Path $root ".vscode/launch.json"
    Tasks = Join-Path $root ".vscode/tasks.json"
    Manifest = Join-Path $root "tools/vscode-bazel-helper/package.json"
    Extension = Join-Path $root "tools/vscode-bazel-helper/src/extension.js"
    Readme = Join-Path $root "tools/vscode-bazel-helper/README.md"
    Helper = Join-Path $root "scripts/commands/ide-build.ps1"
    WorkflowDoc = Join-Path $root "docs/ai-context/build-system/ide-bazel-workflow.md"
}

$scriptCommands = @(
    @{ Id = "bytetrain.bazelHelper.buildApp"; Title = "Bazel: Build App"; Target = "app"; Task = "Bazel: build app" },
    @{ Id = "bytetrain.bazelHelper.runApp"; Title = "Bazel: Run App"; Target = "run-app"; Task = "Bazel: run app" },
    @{ Id = "bytetrain.bazelHelper.assembleDebug"; Title = "Gradle: Assemble Debug"; Target = "gradle-app"; Task = "Gradle: assemble debug" },
    @{ Id = "bytetrain.bazelHelper.buildProto"; Title = "Bazel: Build Proto"; Target = "proto"; Task = "Bazel: build proto" },
    @{ Id = "bytetrain.bazelHelper.buildFeatures"; Title = "Bazel: Build Features"; Target = "features"; Task = "Bazel: build features" },
    @{ Id = "bytetrain.bazelHelper.testRustSdk"; Title = "Rust: Test SDK"; Target = "rust"; Task = "Rust: test SDK" },
    @{ Id = "bytetrain.bazelHelper.queryAppDeps"; Title = "Bazel: Query App Deps"; Target = "query-app-deps"; Task = "Bazel: query app deps" }
)

$utilityCommands = @(
    @{ Id = "bytetrain.bazelHelper.copyDiagnosticContext"; Title = "Bazel: Copy Diagnostic Context" },
    @{ Id = "bytetrain.bazelHelper.openBuildCommands"; Title = "Bazel: Open Build Commands" },
    @{ Id = "bytetrain.bazelHelper.openModuleBoundaries"; Title = "Bazel: Open Module Boundaries" },
    @{ Id = "bytetrain.bazelHelper.openCommonBuildErrors"; Title = "Bazel: Open Common Build Errors" }
)

$requiredExtensions = @(
    "fwcd.kotlin",
    "rust-lang.rust-analyzer",
    "BazelBuild.vscode-bazel",
    "zxh404.vscode-proto3"
)

$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $script:failures.Add($Message)
}

function Read-Text {
    param([string] $Path, [string] $Name)
    if (-not (Test-Path $Path)) {
        Add-Failure "$Name exists"
        return ""
    }
    return Get-Content -Encoding UTF8 -Raw $Path
}

function Read-Json {
    param([string] $Text, [string] $Name)
    try {
        return $Text | ConvertFrom-Json
    } catch {
        Add-Failure "$Name is valid JSON"
        return $null
    }
}

$extensionsText = Read-Text $paths.Extensions ".vscode/extensions.json"
$settingsText = Read-Text $paths.Settings ".vscode/settings.json"
$tasksText = Read-Text $paths.Tasks ".vscode/tasks.json"
$manifestText = Read-Text $paths.Manifest "VS Code extension manifest"
$extensionText = Read-Text $paths.Extension "VS Code extension entry"
$readmeText = Read-Text $paths.Readme "VS Code extension README"
$helperText = Read-Text $paths.Helper "IDE build helper"
$docText = Read-Text $paths.WorkflowDoc "IDE workflow document"

$extensions = Read-Json $extensionsText ".vscode/extensions.json"
$settings = Read-Json $settingsText ".vscode/settings.json"
$tasksJson = Read-Json $tasksText ".vscode/tasks.json"
$manifest = Read-Json $manifestText "VS Code extension manifest"
$null = $settings

if ($null -ne $extensions) {
    $recommendations = @($extensions.recommendations)
    foreach ($id in $requiredExtensions) {
        if ($recommendations -notcontains $id) {
            Add-Failure ".vscode/extensions.json recommends $id"
        }
    }
}

if ($settingsText -match "[A-Za-z]:\\\\" -or $settingsText -match "/Users/" -or $settingsText -match "/home/") {
    Add-Failure ".vscode/settings.json avoids user-local absolute paths"
}

foreach ($term in @("BUILD.bazel", "MODULE.bazel", "*.bzl", "*.proto", "sdk/rust/Cargo.toml")) {
    if ($settingsText -notmatch [regex]::Escape($term)) {
        Add-Failure ".vscode/settings.json records $term"
    }
}

if (Test-Path $paths.Launch) {
    if ($docText -notmatch "VS Code Android debug") {
        Add-Failure "IDE workflow document explains VS Code debug when launch.json exists"
    }
} else {
    foreach ($term in @("Android Studio fallback", "VS Code breakpoint")) {
        if ($docText -notmatch [regex]::Escape($term)) {
            Add-Failure "IDE workflow document records debug fallback term: $term"
        }
    }
}

if ($null -ne $manifest) {
    $declaredCommands = @($manifest.contributes.commands)
    $activationEvents = @($manifest.activationEvents)
    foreach ($command in ($scriptCommands + $utilityCommands)) {
        $declared = $declaredCommands | Where-Object { $_.command -eq $command.Id }
        if ($null -eq $declared) {
            Add-Failure "Manifest declares command $($command.Id)"
        } elseif ($declared.title -ne $command.Title) {
            Add-Failure "Manifest title for $($command.Id) is $($command.Title)"
        }
        if ($activationEvents -notcontains "onCommand:$($command.Id)") {
            Add-Failure "Manifest activationEvents includes onCommand:$($command.Id)"
        }
    }
}

foreach ($command in $scriptCommands) {
    foreach ($term in @($command.Id, "target: `"$($command.Target)`"")) {
        if ($extensionText -notmatch [regex]::Escape($term)) {
            Add-Failure "Extension maps $($command.Id) to $($command.Target)"
            break
        }
    }
    if ($helperText -notmatch [regex]::Escape($command.Target)) {
        Add-Failure "scripts/commands/ide-build.ps1 supports target $($command.Target)"
    }
    if ($readmeText -notmatch [regex]::Escape($command.Id) -or $readmeText -notmatch [regex]::Escape($command.Target)) {
        Add-Failure "README documents $($command.Id) and $($command.Target)"
    }
}

foreach ($command in $utilityCommands) {
    if ($extensionText -notmatch [regex]::Escape($command.Id)) {
        Add-Failure "Extension registers $($command.Id)"
    }
    if ($readmeText -notmatch [regex]::Escape($command.Id)) {
        Add-Failure "README documents $($command.Id)"
    }
}

foreach ($term in @("vscode.env.clipboard.writeText", "lastRun", "exitCode", "Recent output", "openTextDocument", "docs", "ai-context")) {
    if ($extensionText -notmatch [regex]::Escape($term)) {
        Add-Failure "Extension includes diagnostic/document term $term"
    }
}

if ($null -ne $tasksJson) {
    $tasks = @($tasksJson.tasks)
    foreach ($command in $scriptCommands) {
        $matchingTask = $tasks | Where-Object {
            $args = @($_.args)
            $targetIndex = [array]::IndexOf($args, "-Target")
            $hasTarget = $targetIndex -ge 0 -and ($targetIndex + 1) -lt $args.Count -and $args[$targetIndex + 1] -eq $command.Target
            $hasHelper = ($args -join " ") -match [regex]::Escape("scripts/commands/ide-build.ps1")
            $_.label -eq $command.Task -and $_.command -eq "powershell" -and $hasHelper -and $hasTarget
        }
        if ($null -eq $matchingTask) {
            Add-Failure ".vscode/tasks.json exposes task $($command.Task)"
        }
    }

    $debugPrep = $tasks | Where-Object {
        $_.label -eq "Android Studio: prepare debug" -and (@($_.args) -contains "gradle-app")
    }
    if ($null -eq $debugPrep) {
        Add-Failure ".vscode/tasks.json exposes Android Studio debug preparation"
    }
}

foreach ($term in @(
    "bytetrain.bazelHelper.copyDiagnosticContext",
    "bytetrain.bazelHelper.openBuildCommands",
    "Copy Diagnostic Context",
    "Android Studio fallback",
    "Kotlin",
    "Rust",
    "Bazel/Starlark",
    "proto",
    "extensions.json",
    "settings.json"
)) {
    if ($docText -notmatch [regex]::Escape($term)) {
        Add-Failure "IDE workflow document records $term"
    }
}

if ($failures.Count -gt 0) {
    Write-Host "IDE-FULL check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "IDE-FULL check passed."

