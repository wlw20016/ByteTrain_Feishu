## Why

The project currently provides `scripts/ide-build.ps1` and `.vscode/tasks.json`, which satisfy a minimal IDE build entry. The course requirement also mentions producing a VS Code plugin or plugin-shaped capability, so this change scopes a small extension prototype without expanding into a full IDE platform project.

## What Changes

- Add a minimal VS Code extension prototype or plugin-shaped package that invokes the existing IDE build helper.
- Expose commands for app build, proto build, feature build, Rust SDK test, and app dependency query.
- Capture command output, exit code, and working directory in an IDE-visible output channel or equivalent log.
- Document Trae compatibility through VS Code task reuse or extension command reuse.
- Keep `scripts/ide-build.ps1` as the single source of build command truth.

## Capabilities

### New Capabilities

- `ide-build-plugin`: Minimal VS Code/Trae build plugin prototype for invoking Bazel/Rust verification commands and viewing output.

### Modified Capabilities

- None.

## Impact

- `.vscode/`
- optional extension prototype directory under `tools/` or `ide/`
- `scripts/ide-build.ps1`
- `docs/ai-context/ide-bazel-workflow.md`
- IDE verification scripts
