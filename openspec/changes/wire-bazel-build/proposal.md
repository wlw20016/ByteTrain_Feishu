# Proposal: wire-bazel-build

## Why

The repository currently contains placeholder BUILD files only. The course explicitly requires Bazel to manage modules, dependencies, build, tests, query analysis, and engineering evidence.

## What

- Choose and document Bazel rules for Android, Kotlin, proto, and Rust.
- Wire proto, shared, feature, app, and Rust SDK targets incrementally.
- Add build/test/query commands to the AI context docs.
- Record build failures, fixes, query results, and validation evidence.

## Impact

- Affects `MODULE.bazel`, `BUILD.bazel` files, `docs/ai-context`, and possibly generated-source conventions.
- May require local toolchain installation or dependency downloads.
- Creates the formal build-validation path for later acceptance.
