Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$routesPath = Join-Path $root "shared/navigation/AppRoutes.kt"
$source = Get-Content -Encoding UTF8 -Raw $mainActivityPath
$routes = Get-Content -Encoding UTF8 -Raw $routesPath

$checks = @(
    @{
        Name = "MainActivity imports AppRoutes"
        Pattern = "import\s+com\.bytetrain\.feishuclone\.shared\.navigation\.AppRoutes"
    },
    @{
        Name = "MainActivity tracks the selected route"
        Pattern = "currentRoute(?:\s*:\s*String)?\s*=\s*AppRoutes\.MESSAGE_LIST"
    },
    @{
        Name = "MainActivity renders message tab from AppRoutes"
        Pattern = "AppRoutes\.MESSAGE_LIST"
    },
    @{
        Name = "MainActivity renders mail tab from AppRoutes"
        Pattern = "AppRoutes\.MAIL_LIST"
    },
    @{
        Name = "MainActivity switches tabs on click"
        Pattern = "setOnClickListener\s*\{[\s\S]*selectRoute\s*\("
    },
    @{
        Name = "MainActivity updates selected tab content"
        Pattern = "renderSelectedRoute\s*\("
    },
    @{
        Name = "MainActivity includes bottom tab bar builder"
        Pattern = "createBottomTabBar\s*\("
    }
)

$failures = New-Object System.Collections.Generic.List[string]
foreach ($check in $checks) {
    if ($source -notmatch $check.Pattern) {
        $failures.Add($check.Name)
    }
}

if ($routes -notmatch 'const\s+val\s+MESSAGE_LIST\s*=\s*"message_list"') {
    $failures.Add("AppRoutes keeps MESSAGE_LIST route stable")
}

if ($routes -notmatch 'const\s+val\s+MAIL_LIST\s*=\s*"mail_list"') {
    $failures.Add("AppRoutes keeps MAIL_LIST route stable")
}

if ($failures.Count -gt 0) {
    Write-Host "UI-002 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI-002 check passed."

