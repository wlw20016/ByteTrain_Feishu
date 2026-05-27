#!/usr/bin/env python3
"""Validate ByteTrain commit messages."""

from __future__ import annotations

import re
import sys
from pathlib import Path


ALLOWED_TYPES = ("feat", "fix", "docs", "test", "refactor", "chore", "build", "ci")
ALLOWED_SCOPES = (
    "app",
    "message",
    "mail",
    "shared",
    "proto",
    "bazel",
    "openspec",
    "docs",
    "sdk",
)

PATTERN = re.compile(
    rf"^({'|'.join(ALLOWED_TYPES)})\(({'|'.join(ALLOWED_SCOPES)})\): [a-z0-9].{{4,71}}$"
)


def main() -> int:
    if len(sys.argv) != 2:
        print("ERROR: commit-msg hook requires the commit message file path.", file=sys.stderr)
        return 1

    message_path = Path(sys.argv[1])
    first_line = message_path.read_text(encoding="utf-8-sig").splitlines()[0].strip()

    bypass_prefixes = ("Merge ", "Revert ", "fixup!", "squash!")
    if first_line.startswith(bypass_prefixes):
        return 0

    if PATTERN.fullmatch(first_line):
        return 0

    print("ERROR: commit message does not match ByteTrain convention.", file=sys.stderr)
    print("", file=sys.stderr)
    print("Expected:", file=sys.stderr)
    print("  <type>(<scope>): <subject>", file=sys.stderr)
    print("", file=sys.stderr)
    print(f"Allowed type: {', '.join(ALLOWED_TYPES)}", file=sys.stderr)
    print(f"Allowed scope: {', '.join(ALLOWED_SCOPES)}", file=sys.stderr)
    print("", file=sys.stderr)
    print("Examples:", file=sys.stderr)
    print("  feat(message): add paged message list", file=sys.stderr)
    print("  docs(openspec): add message pagination change", file=sys.stderr)
    print("  fix(bazel): fix proto visibility", file=sys.stderr)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
