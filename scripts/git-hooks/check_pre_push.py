#!/usr/bin/env python3
"""Run ByteTrain build and test checks before pushing."""

from __future__ import annotations

import os
import shutil
import subprocess
import sys


def run(command: list[str]) -> int:
    print(f"+ {' '.join(command)}")
    return subprocess.run(command).returncode


def main() -> int:
    if os.getenv("BYTETRAIN_SKIP_PRE_PUSH") == "1":
        print("Skipping pre-push checks because BYTETRAIN_SKIP_PRE_PUSH=1.")
        return 0

    bazel = shutil.which("bazel") or shutil.which("bazelisk")
    if not bazel:
        print("ERROR: bazel or bazelisk is required for pre-push checks.", file=sys.stderr)
        print("Install Bazel/Bazelisk, or set BYTETRAIN_SKIP_PRE_PUSH=1 for an emergency.", file=sys.stderr)
        return 1

    build_status = run([bazel, "build", "//..."])
    if build_status != 0:
        return build_status

    test_status = run([bazel, "test", "//..."])
    if test_status != 0:
        return test_status

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
