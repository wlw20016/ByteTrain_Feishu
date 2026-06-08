Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$root = Resolve-Path (Join-Path $PSScriptRoot "..\..\..")

$scanRoots = @(
    "app",
    "features",
    "shared",
    "docs",
    "openspec",
    "scripts"
)

$excludedPathFragments = @(
    "\openspec\changes\archive\",
    "\.git\",
    "\.gradle\",
    "\build\",
    "\bazel-bin\",
    "\bazel-out\",
    "\bazel-testlogs\"
)

$mojibakeCodePoints = @(
    0xFFFD,
    0x95BF,
    0x93E0,
    0x6769,
    0x6FC2,
    0x951B,
    0x7AD4,
    0x95AD,
    0x9225,
    0x9435,
    0x5A11,
    0x7ECB,
    0x9359,
    0x9352,
    0x6952,
    0x20AC
)

$extensions = @(
    ".kt",
    ".kts",
    ".xml",
    ".md",
    ".ps1",
    ".json",
    ".bazel",
    ".bzl",
    ".proto",
    ".rs",
    ".txt"
)

$failures = New-Object System.Collections.Generic.List[string]

foreach ($relativeRoot in $scanRoots) {
    $absoluteRoot = Join-Path $root $relativeRoot
    if (-not (Test-Path $absoluteRoot)) {
        continue
    }

    Get-ChildItem -LiteralPath $absoluteRoot -Recurse -File | ForEach-Object {
        $file = $_
        $normalizedPath = $file.FullName.Replace("/", "\")
        if ($extensions -notcontains $file.Extension.ToLowerInvariant()) {
            return
        }
        foreach ($fragment in $excludedPathFragments) {
            if ($normalizedPath.Contains($fragment)) {
                return
            }
        }

        $text = Get-Content -Encoding UTF8 -Raw -LiteralPath $file.FullName
        foreach ($codePoint in $mojibakeCodePoints) {
            $marker = [char]$codePoint
            if ($text.Contains($marker)) {
                $relativePath = Resolve-Path -Relative -LiteralPath $file.FullName
                $failures.Add("$relativePath contains mojibake marker U+$($codePoint.ToString('X4'))")
                break
            }
        }
    }
}

if ($failures.Count -gt 0) {
    Write-Host "TEXT-ENC check failed:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

Write-Host "TEXT-ENC check passed."
