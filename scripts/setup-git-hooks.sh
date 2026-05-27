#!/usr/bin/env sh
set -eu

ROOT="$(git rev-parse --show-toplevel)"
cd "$ROOT"

git config core.hooksPath .githooks

echo "ByteTrain Git hooks enabled: core.hooksPath=.githooks"
echo "Commit checks: pre-commit + commit-msg"
echo "Push checks: bazel build //... + bazel test //..."
