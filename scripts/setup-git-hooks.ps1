$ErrorActionPreference = "Stop"

$root = git rev-parse --show-toplevel
Set-Location $root

git config core.hooksPath .githooks

Write-Host "ByteTrain Git hooks enabled: core.hooksPath=.githooks"
Write-Host "Commit checks: pre-commit + commit-msg"
Write-Host "Push checks: bazel build //... + bazel test //..."
