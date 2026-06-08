# Design: Verify Full Bazel Workspace

## Current State

Target-level verification is strong, and `bazel run //app:run_app` has been validated. Full wildcard verification remains a design goal in `build-commands.md`, not a proven result.

## Approach

- Add a script such as `scripts/commands/verify-full-bazel-workspace.ps1`.
- Run:
  - `bazel --batch build //... --curses=no --show_progress_rate_limit=60 --jobs=4`,
  - `bazel --batch test //... --curses=no --show_progress_rate_limit=60 --jobs=4`,
  - a focused `bazel query` summary.
- If `//...` includes non-testable run wrappers or environment-dependent targets, explicitly document exclusions and prefer target tags or query filters rather than silent omission.
- Capture elapsed time, status, command, and output summary in `docs/evidence`.
- Update `common-build-errors.md` for any new failures and fixes.

## Validation

- Full workspace verification script exits zero when the chosen acceptance target set passes.
- Evidence file records exact commands and summaries.
- `docs/ai-context/build-system/build-commands.md` distinguishes verified full-workspace commands from target-level commands.
