$ErrorActionPreference = "Stop"

$root = git rev-parse --show-toplevel
Set-Location $root

git config core.hooksPath .githooks

Write-Host "ByteTrain Git hooks enabled: core.hooksPath=.githooks"
Write-Host "Commit checks: pre-commit + commit-msg"
Write-Host "Push checks: bazel build //... + bazel test //..."

$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) {
    $python = Get-Command py -ErrorAction SilentlyContinue
}

if (-not $python) {
    Write-Warning "Python was not found. Commit hooks need Python 3 on PATH."
}

$bazel = Get-Command bazel -ErrorAction SilentlyContinue
if (-not $bazel) {
    $bazel = Get-Command bazelisk -ErrorAction SilentlyContinue
}

if (-not $bazel) {
    Write-Warning "Bazel/Bazelisk was not found. Push hooks will block until one is installed, unless BYTETRAIN_SKIP_PRE_PUSH=1 is set."
}
