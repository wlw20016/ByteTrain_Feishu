## Why

The repository records historical Bazel success for app, proto, feature, and query targets, but final course acceptance needs a current, repeatable Bazel verification package. This change creates a final Bazel delivery checklist and closes environment blockers such as local Bazel access or stale process issues.

## What Changes

- Define the final Bazel verification command set for the Android-only project.
- Add a focused script or documented procedure that runs the final build/test/query checks.
- Record current command outputs, failures, root causes, and retry instructions.
- Treat Gradle as historical/transition evidence only, not final acceptance.
- Skip iOS/Xcode-specific requirements explicitly.

## Capabilities

### New Capabilities

- `bazel-delivery-verification`: Final Bazel verification for Android app, Kotlin feature modules, proto contracts, Rust SDK tests, and dependency query evidence.

### Modified Capabilities

- None.

## Impact

- `scripts/`
- `docs/ai-context/build-system/build-commands.md`
- `docs/ai-context/build-system/common-build-errors.md`
- `docs/project/module-boundaries.md`
- Bazel output artifacts and verification records
