Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$pagingPath = Join-Path $root "shared/list/PagingModels.kt"
$matrixPath = Join-Path $root "docs/ai-context/ui-state-matrix.md"

$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path $pagingPath)) {
    $failures.Add("PagingModels.kt exists")
} else {
    $paging = Get-Content -Encoding UTF8 -Raw $pagingPath
    $statePatterns = @{
        Loading = "data\s+object\s+Loading\s*:"
        Empty = "data\s+object\s+Empty\s*:"
        Error = "data\s+class\s+Error\s*\("
        Content = "data\s+class\s+Content\s*<T>\s*\("
        LoadingMore = "data\s+class\s+LoadingMore\s*<T>\s*\("
        LoadMoreError = "data\s+class\s+LoadMoreError\s*<T>\s*\("
    }

    foreach ($state in $statePatterns.Keys) {
        if ($paging -notmatch $statePatterns[$state]) {
            $failures.Add("PagingUiState declares $state")
        }
    }

    if ($paging -notmatch "LoadingMore[\s\S]*val\s+items\s*:\s*List<T>") {
        $failures.Add("LoadingMore preserves loaded items")
    }

    if ($paging -notmatch "LoadMoreError[\s\S]*val\s+items\s*:\s*List<T>") {
        $failures.Add("LoadMoreError preserves loaded items")
    }

    if ($paging -notmatch "LoadMoreError[\s\S]*val\s+message\s*:\s*String") {
        $failures.Add("LoadMoreError carries an error message")
    }
}

if (-not (Test-Path $matrixPath)) {
    $failures.Add("UI state matrix document exists")
} else {
    $matrix = Get-Content -Encoding UTF8 -Raw $matrixPath
    foreach ($state in @("Loading", "Empty", "Error", "Content", "LoadingMore", "LoadMoreError")) {
        $stateToken = [regex]::Escape("``$state``")
        if ($matrix -notmatch $stateToken) {
            $failures.Add("UI state matrix documents $state")
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "TEST-003 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "TEST-003 check passed."
