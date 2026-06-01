Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..")
$mainActivityPath = Join-Path $root "app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt"
$manifestPath = Join-Path $root "app/src/main/AndroidManifest.xml"
$source = Get-Content -Encoding UTF8 -Raw $mainActivityPath

$checks = @(
    @{
        Name = "MainActivity imports android.app.Activity"
        Pattern = "import\s+android\.app\.Activity"
    },
    @{
        Name = "MainActivity imports android.os.Bundle"
        Pattern = "import\s+android\.os\.Bundle"
    },
    @{
        Name = "MainActivity extends Activity"
        Pattern = "class\s+MainActivity\s*:\s*Activity\s*\("
    },
    @{
        Name = "MainActivity overrides onCreate"
        Pattern = "override\s+fun\s+onCreate\s*\(\s*savedInstanceState\s*:\s*Bundle\?\s*\)"
    },
    @{
        Name = "MainActivity calls super.onCreate"
        Pattern = "super\.onCreate\s*\(\s*savedInstanceState\s*\)"
    },
    @{
        Name = "MainActivity installs a root view"
        Pattern = "setContentView\s*\("
    }
)

$failures = New-Object System.Collections.Generic.List[string]
foreach ($check in $checks) {
    if ($source -notmatch $check.Pattern) {
        $failures.Add($check.Name)
    }
}

if ($source -match "placeholder|will be added after") {
    $failures.Add("MainActivity no longer contains placeholder wording")
}

if (-not (Test-Path $manifestPath)) {
    $failures.Add("AndroidManifest.xml declares the app entry activity")
} else {
    $manifest = Get-Content -Encoding UTF8 -Raw $manifestPath
    $manifestChecks = @(
        @{
            Name = "Manifest declares MainActivity"
            Pattern = 'android:name="\.MainActivity"'
        },
        @{
            Name = "Manifest marks MainActivity exported"
            Pattern = 'android:exported="true"'
        },
        @{
            Name = "Manifest declares launcher action"
            Pattern = 'android\.intent\.action\.MAIN'
        },
        @{
            Name = "Manifest declares launcher category"
            Pattern = 'android\.intent\.category\.LAUNCHER'
        }
    )

    foreach ($check in $manifestChecks) {
        if ($manifest -notmatch $check.Pattern) {
            $failures.Add($check.Name)
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "UI-001 check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "UI-001 check passed."
