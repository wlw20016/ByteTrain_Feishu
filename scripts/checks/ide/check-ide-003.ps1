Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$manifestPath = Join-Path $root "tools/vscode-bazel-helper/package.json"
$extensionPath = Join-Path $root "tools/vscode-bazel-helper/src/extension.js"
$readmePath = Join-Path $root "tools/vscode-bazel-helper/README.md"
$tasksJsonPath = Join-Path $root ".vscode/tasks.json"
$helperPath = Join-Path $root "scripts/commands/ide-build.ps1"
$docPath = Join-Path $root "docs/ai-context/build-system/ide-bazel-workflow.md"

$expected = @(
    @{ Id = "bytetrain.bazelHelper.buildApp"; Title = "Bazel: Build App"; Target = "app" },
    @{ Id = "bytetrain.bazelHelper.runApp"; Title = "Bazel: Run App"; Target = "run-app" },
    @{ Id = "bytetrain.bazelHelper.assembleDebug"; Title = "Gradle: Assemble Debug"; Target = "gradle-app" },
    @{ Id = "bytetrain.bazelHelper.buildProto"; Title = "Bazel: Build Proto"; Target = "proto" },
    @{ Id = "bytetrain.bazelHelper.buildFeatures"; Title = "Bazel: Build Features"; Target = "features" },
    @{ Id = "bytetrain.bazelHelper.testRustSdk"; Title = "Rust: Test SDK"; Target = "rust" },
    @{ Id = "bytetrain.bazelHelper.queryAppDeps"; Title = "Bazel: Query App Deps"; Target = "query-app-deps" }
)

$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $script:failures.Add($Message)
}

function Read-RequiredText {
    param(
        [string] $Path,
        [string] $Name
    )

    if (-not (Test-Path $Path)) {
        Add-Failure "$Name exists"
        return ""
    }

    return Get-Content -Encoding UTF8 -Raw $Path
}

$manifestText = Read-RequiredText -Path $manifestPath -Name "VS Code extension manifest"
$extensionText = Read-RequiredText -Path $extensionPath -Name "VS Code extension entry"
$readmeText = Read-RequiredText -Path $readmePath -Name "VS Code extension README"
$tasksJsonText = Read-RequiredText -Path $tasksJsonPath -Name ".vscode/tasks.json"
$helperText = Read-RequiredText -Path $helperPath -Name "IDE build helper"
$docText = Read-RequiredText -Path $docPath -Name "IDE workflow document"

$manifest = $null
if ($manifestText.Length -gt 0) {
    try {
        $manifest = $manifestText | ConvertFrom-Json
    } catch {
        Add-Failure "VS Code extension manifest is valid JSON"
    }
}

$tasksJson = $null
if ($tasksJsonText.Length -gt 0) {
    try {
        $tasksJson = $tasksJsonText | ConvertFrom-Json
    } catch {
        Add-Failure ".vscode/tasks.json is valid JSON"
    }
}

if ($null -ne $manifest) {
    if ($manifest.main -ne "./src/extension.js") {
        Add-Failure "VS Code extension manifest points to ./src/extension.js"
    }

    $declaredCommands = @($manifest.contributes.commands)
    $activationEvents = @($manifest.activationEvents)
    foreach ($command in $expected) {
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

foreach ($command in $expected) {
    foreach ($term in @($command.Id, "target: `"$($command.Target)`"")) {
        if ($extensionText -notmatch [regex]::Escape($term)) {
            Add-Failure "Extension entry maps $($command.Id) to target $($command.Target)"
            break
        }
    }

    if ($readmeText -notmatch [regex]::Escape($command.Id) -or
        $readmeText -notmatch [regex]::Escape($command.Target)) {
        Add-Failure "Extension README documents $($command.Id) and $($command.Target)"
    }
}

foreach ($term in @(
    "createOutputChannel(`"ByteTrain Bazel Helper`")",
    "scripts",
    "ide-build.ps1",
    "-Target",
    "stdout",
    "stderr",
    "Exit code"
)) {
    if ($extensionText -notmatch [regex]::Escape($term)) {
        Add-Failure "Extension entry includes $term"
    }
}

foreach ($target in ($expected | ForEach-Object { $_.Target })) {
    if ($helperText -notmatch [regex]::Escape($target)) {
        Add-Failure "scripts/commands/ide-build.ps1 supports target $target"
    }
}

if ($null -ne $tasksJson) {
    $tasks = @($tasksJson.tasks)
    foreach ($target in ($expected | ForEach-Object { $_.Target })) {
        $matchingTask = $tasks | Where-Object {
            $args = @($_.args)
            $targetIndex = [array]::IndexOf($args, "-Target")
            $hasTarget = $targetIndex -ge 0 -and ($targetIndex + 1) -lt $args.Count -and $args[$targetIndex + 1] -eq $target
            $hasHelper = ($args -join " ") -match [regex]::Escape("scripts/commands/ide-build.ps1")
            $_.command -eq "powershell" -and $hasHelper -and $hasTarget
        }

        if ($null -eq $matchingTask) {
            Add-Failure ".vscode/tasks.json exposes scripts/commands/ide-build.ps1 target $target"
        }
    }
}

foreach ($term in @(
    "tools/vscode-bazel-helper",
    "ByteTrain Bazel Helper",
    "bytetrain.bazelHelper.buildApp",
    "bytetrain.bazelHelper.queryAppDeps",
    "Trae",
    ".vscode/tasks.json",
    "scripts/commands/ide-build.ps1"
)) {
    if ($docText -notmatch [regex]::Escape($term)) {
        Add-Failure "IDE workflow document records plugin term $term"
    }
}

if ($failures.Count -gt 0) {
    Write-Host "IDE-PLUG check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "IDE-PLUG check passed."

