## Overview

This change establishes final Bazel verification as the source of truth for course acceptance. Gradle remains a development convenience and historical transition artifact, but final verification uses Bazel commands.

## Final Command Set

The verification package should cover:

- App build: `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4`
- Proto build: `bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4`
- Shared/feature Kotlin build: the target set currently recorded in `docs/ai-context/build-system/build-commands.md`
- Rust SDK Bazel test: `bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4`
- Dependency query: `bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"`

## Verification Script

A script may be added to run these commands in sequence and write a concise evidence file. It must:

- Stop on command failure.
- Record the exact command.
- Record pass/fail and short output summaries.
- Avoid storing large build logs in OpenSpec tasks.

## Environment Blockers

If a command fails due to local environment rather than project code, the failure must be documented with:

- command
- observed output
- likely root cause
- remediation
- retry command

Examples include Bazel shim `Access is denied`, stale Bazel server/processes, network download timeouts, Android SDK location, JDK version, and repository cache/distdir issues.

## Acceptance Boundary

iOS, UIKit, AutoLayout, and Xcode project generation are out of scope for this Android-only repository unless a future change explicitly adds iOS support.
