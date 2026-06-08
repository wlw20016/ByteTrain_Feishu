Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$docPath = Join-Path $root "docs/ai-context/ui/ui-main-flow-doc-001.md"
$tasksPath = Join-Path $root "openspec/changes/add-ui-main-flow/tasks.md"
if (-not (Test-Path $tasksPath)) {
    $tasksPath = Join-Path $root "openspec/changes/archive/2026-06-06-add-ui-main-flow/tasks.md"
}

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $docPath)) {
    $failures.Add("DOC-001 evidence document exists")
} else {
    $doc = Get-Content -Encoding UTF8 -Raw $docPath
    foreach ($term in @(
        "AI prompt",
        "AI conclusions",
        "Human decisions",
        "Final result",
        "add-ui-main-flow",
        "Messages",
        "Mail",
        ":app:assembleDebug",
        "OpenSpec"
    )) {
        if ($doc -notmatch [regex]::Escape($term)) {
            $failures.Add("DOC-001 document records $term")
        }
    }
}

if (-not (Test-Path $tasksPath)) {
    $failures.Add("add-ui-main-flow tasks.md exists")
} else {
    $tasks = Get-Content -Encoding UTF8 -Raw $tasksPath
    if ($tasks -notmatch "\[x\]\s+DOC-001") {
        $failures.Add("DOC-001 is marked complete")
    }
    if ($tasks -notmatch "ui-main-flow-doc-001\.md") {
        $failures.Add("DOC-001 records document evidence")
    }
    if ($tasks -notmatch "check-doc-001\.ps1") {
        $failures.Add("DOC-001 records check-doc-001.ps1 evidence")
    }
}

if ($failures.Count -gt 0) {
    Write-Host "DOC-001 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "DOC-001 check passed."

