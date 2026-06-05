Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$helperPath = Join-Path $root "scripts/ide-build.ps1"
$tasksJsonPath = Join-Path $root ".vscode/tasks.json"
$gitignorePath = Join-Path $root ".gitignore"
$docPath = Join-Path $root "docs/ai-context/ide-bazel-workflow.md"
$tasksPath = Join-Path $root "openspec/changes/improve-ai-context/tasks.md"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $helperPath)) {
    $failures.Add("IDE build helper exists")
} else {
    $helper = Get-Content -Encoding UTF8 -Raw $helperPath
    foreach ($term in @(
        'ValidateSet("app", "gradle-app", "proto", "features", "rust", "query-app-deps")',
        'bazel',
        '//app:app',
        '//proto:...',
        '//features/message:message',
        '//features/mail:mail',
        'cargo',
        'sdk/rust/Cargo.toml',
        'deps(//app:app, 2)'
    )) {
        if ($helper -notmatch [regex]::Escape($term)) {
            $failures.Add("IDE build helper includes $term")
        }
    }
}

if (-not (Test-Path $tasksJsonPath)) {
    $failures.Add(".vscode/tasks.json exists")
} else {
    $tasksJson = Get-Content -Encoding UTF8 -Raw $tasksJsonPath
    $json = $null
    try {
        $json = $tasksJson | ConvertFrom-Json
    } catch {
        $failures.Add(".vscode/tasks.json is valid JSON")
    }

    foreach ($term in @(
        "Bazel: build app",
        "Gradle: assemble debug",
        "Bazel: build proto",
        "Bazel: build features",
        "Rust: test SDK",
        "Bazel: query app deps",
        "scripts/ide-build.ps1"
    )) {
        if ($tasksJson -notmatch [regex]::Escape($term)) {
            $failures.Add(".vscode/tasks.json includes $term")
        }
    }

    if ($null -ne $json -and $json.tasks.Count -lt 6) {
        $failures.Add(".vscode/tasks.json exposes at least six IDE tasks")
    }
}

if (-not (Test-Path $gitignorePath)) {
    $failures.Add(".gitignore exists")
} else {
    $gitignore = Get-Content -Encoding UTF8 -Raw $gitignorePath
    foreach ($term in @(".vscode/", "!.vscode/", ".vscode/*", "!.vscode/tasks.json")) {
        if ($gitignore -notmatch [regex]::Escape($term)) {
            $failures.Add(".gitignore allows shared .vscode/tasks.json while ignoring personal VS Code files")
            break
        }
    }
}

if (-not (Test-Path $docPath)) {
    $failures.Add("IDE workflow document exists")
} else {
    $doc = Get-Content -Encoding UTF8 -Raw $docPath
    foreach ($term in @(
        "scripts/ide-build.ps1",
        ".vscode/tasks.json",
        "Bazel: build app",
        "Gradle: assemble debug",
        "Bazel: build proto",
        "Bazel: build features",
        "Rust: test SDK",
        "Bazel: query app deps",
        "IDE-002"
    )) {
        if ($doc -notmatch [regex]::Escape($term)) {
            $failures.Add("IDE workflow document records $term")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("improve-ai-context tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+IDE-002") {
        $failures.Add("IDE-002 is marked complete")
    }
    foreach ($term in @("scripts/ide-build.ps1", ".vscode/tasks.json", "check-ide-002.ps1")) {
        if ($tasks -notmatch [regex]::Escape($term)) {
            $failures.Add("IDE-002 records $term evidence")
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "IDE-002 check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "IDE-002 check passed."
