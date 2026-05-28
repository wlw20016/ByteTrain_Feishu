# Design: wire-bazel-build

## Incremental Build Strategy

Bazel should be connected from small stable targets to the full app:

1. Proto targets.
2. Shared Kotlin targets.
3. Feature Kotlin targets.
4. Android app target.
5. Rust SDK target.
6. Query and dependency-boundary evidence.

## Target Boundary

Expected target groups:

- `//proto:...`
- `//shared/list:...`
- `//shared/navigation:...`
- `//features/message:...`
- `//features/mail:...`
- `//app:app`
- `//sdk/rust:...`

## Documentation

Every build change should update or verify:

- `docs/ai-context/build-commands.md`
- `docs/ai-context/module-boundaries.md`
- `docs/ai-context/common-build-errors.md`

## Query Evidence

Use `bazel query`, `cquery`, or `aquery` when useful to prove module dependency boundaries and diagnose failures.

## Risk

Android + Kotlin + Rust + proto Bazel integration can be time-consuming. The change should prefer incremental, reviewable steps over one large toolchain rewrite.
