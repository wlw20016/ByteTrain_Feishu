#!/usr/bin/env python3
"""Run lightweight ByteTrain checks before a commit is created."""

from __future__ import annotations

import os
import subprocess
import sys
from pathlib import PurePosixPath


SOURCE_PREFIXES = ("app/", "features/", "shared/", "proto/", "sdk/")
FORBIDDEN_SUFFIXES = (".log", ".tmp", ".bak", ".swp")
FORBIDDEN_NAMES = {".DS_Store", "Thumbs.db"}


def git(*args: str) -> str:
    result = subprocess.run(
        ("git", *args),
        check=True,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
    )
    return result.stdout.strip()


def staged_files() -> list[str]:
    output = git("diff", "--cached", "--name-only", "--diff-filter=ACMRT")
    return [line.replace("\\", "/") for line in output.splitlines() if line.strip()]


def current_branch() -> str:
    try:
        return git("symbolic-ref", "--quiet", "--short", "HEAD")
    except subprocess.CalledProcessError:
        return "HEAD"


def has_forbidden_file(path: str) -> bool:
    name = PurePosixPath(path).name
    return name in FORBIDDEN_NAMES or path.endswith(FORBIDDEN_SUFFIXES) or "__pycache__/" in path


def main() -> int:
    branch = current_branch()
    if branch in {"main", "master"} and os.getenv("BYTETRAIN_ALLOW_MAIN_COMMIT") != "1":
        print("ERROR: direct commits to main/master are blocked.", file=sys.stderr)
        print("Create a feature branch, or set BYTETRAIN_ALLOW_MAIN_COMMIT=1 for an emergency.", file=sys.stderr)
        return 1

    files = staged_files()
    if not files:
        return 0

    forbidden = [path for path in files if has_forbidden_file(path)]
    if forbidden:
        print("ERROR: forbidden temporary/generated files are staged:", file=sys.stderr)
        for path in forbidden:
            print(f"  - {path}", file=sys.stderr)
        return 1

    source_changed = any(path.startswith(SOURCE_PREFIXES) for path in files)
    openspec_changed = any(path.startswith("openspec/changes/") for path in files)
    if (
        source_changed
        and not openspec_changed
        and os.getenv("BYTETRAIN_SKIP_OPENSPEC_CHECK") != "1"
    ):
        print("ERROR: source changes require a related OpenSpec change in openspec/changes/.", file=sys.stderr)
        print("Stage proposal/design/tasks/spec updates, or set BYTETRAIN_SKIP_OPENSPEC_CHECK=1 if this is truly exempt.", file=sys.stderr)
        return 1

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
