# Tasks: wire-bazel-build

## 1. Toolchain Decision

- [ ] BZL-001 Decide Bazel rules for Android, Kotlin, proto, and Rust; record accepted/rejected options.

## 2. Targets

- [ ] BZL-002 Add proto Bazel targets and validate `bazel build //proto:...`.
- [ ] BZL-003 Add Kotlin targets for shared, message, and mail modules.
- [ ] BZL-004 Add Android app target and validate the app build target.
- [ ] BZL-005 Add Rust SDK Bazel target and validate Rust tests through Bazel if feasible.

## 3. Query and Observability

- [ ] BZL-006 Run Bazel query for app/module dependencies and record output summary.
- [ ] Document build commands in `docs/ai-context/build-commands.md`.
- [ ] Document dependency boundaries in `docs/ai-context/module-boundaries.md`.
- [ ] Document real build failures and fixes in `docs/ai-context/common-build-errors.md`.

## 4. Evidence

- [ ] Record successful build/test command outputs or summaries in this `tasks.md`.
- [ ] Update Feishu Bitable rows for Bazel tasks with evidence links.
