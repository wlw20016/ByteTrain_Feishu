# Change: Verify Full Bazel Workspace

## Why

The project has verified important Bazel targets individually: app, proto, feature/shared targets, Rust SDK test, query, and run. However, strict full-workspace commands such as `bazel build //...` and `bazel test //...` have not been proven as final acceptance commands. The requirement asks for Bazel managing modules, dependencies, build, test, and artifacts; full-workspace verification closes that gap or documents precise blockers.

## What Changes

- Add final full-workspace Bazel build and test verification.
- Record whether `bazel build //...` and `bazel test //...` pass, or document any excluded targets and why.
- Add a repeatable script entry for full-workspace verification.
- Update build evidence and common-error docs with command output summaries.

## Impact

- Affects scripts and docs/evidence.
- May require BUILD target fixes if wildcard verification exposes hidden issues.
- Does not change app behavior unless uncovered build issues require fixes.
