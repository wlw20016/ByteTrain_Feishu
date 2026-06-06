Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$acceptancePath = Join-Path $root "docs/ai-context/ui/ui-main-flow-release-evidence.md"
$tasksPath = Join-Path $root "openspec/changes/add-ui-main-flow/tasks.md"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $acceptancePath)) {
    $failures.Add("UI main flow release evidence exists")
} else {
    $evidence = Get-Content -Encoding UTF8 -Raw $acceptancePath
    foreach ($term in @(
        ":app:assembleDebug",
        "check-ui-001.ps1",
        "check-ui-002.ps1",
        "check-ui-003.ps1",
        "check-msg-006.ps1",
        "check-mail-006.ps1",
        "check-test-001.ps1",
        "check-test-002.ps1",
        "check-test-003.ps1",
        "check-test-004.ps1",
        "openspec.cmd validate add-ui-main-flow --strict",
        "manual acceptance"
    )) {
        if ($evidence -notmatch [regex]::Escape($term)) {
            $failures.Add("Release evidence documents $term")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("add-ui-main-flow tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+REL-001") {
        $failures.Add("REL-001 is marked complete")
    }
    if ($tasks -notmatch "check-rel-001\.ps1") {
        $failures.Add("REL-001 records check-rel-001.ps1 evidence")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "REL-001 check failed:"
    foreach ($failure in $failures) { Write-Host " - $failure" }
    exit 1
}

Write-Host "REL-001 check passed."

