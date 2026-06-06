## Overview

This change adds a plugin-shaped IDE integration while preserving the current script-first architecture. The plugin prototype must call `scripts/commands/ide-build.ps1` rather than duplicating Bazel commands.

## Prototype Shape

Preferred implementation:

- Create a small VS Code extension prototype under `tools/vscode-bazel-helper/`.
- Provide `package.json` command declarations.
- Provide an extension entry that runs PowerShell with `scripts/commands/ide-build.ps1 -Target <name>`.
- Write command output to a named output channel.
- Surface non-zero exit codes clearly.

Acceptable minimal alternative:

- If full extension scaffolding is too heavy, create a documented plugin-shaped command manifest and launcher script that a VS Code/Trae extension can call, but tasks must remain incomplete until a runnable prototype is present.

## Commands

The prototype should expose:

- Build app
- Assemble debug through Gradle only as optional transition command
- Build proto
- Build features
- Test Rust SDK
- Query app deps

## Trae Compatibility

Trae compatibility should be documented as one of:

- Trae reads `.vscode/tasks.json`.
- Trae can invoke the same extension commands.
- Trae can call `scripts/commands/ide-build.ps1` directly.

## Verification

Verification should include:

- JSON/package manifest validation.
- Static check that commands call `scripts/commands/ide-build.ps1`.
- At least one smoke command that runs a lightweight target such as `query-app-deps`, unless local Bazel environment blocks it.
- Documentation update in `docs/ai-context/build-system/ide-bazel-workflow.md`.
