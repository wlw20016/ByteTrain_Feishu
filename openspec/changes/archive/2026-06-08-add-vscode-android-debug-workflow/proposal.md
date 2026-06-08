# Change: Add VS Code Android Debug Workflow

## Why

Before this change, the IDE workflow supported build, run, indexing, command output, and diagnostic context. It explicitly did not claim VS Code breakpoint support, with Android Studio fallback documented instead. The requirement asks for Trae/VS Code IDE compile, run, indexing, breakpoints, and code completion support. This change closes that gap with an explicit VS Code Android JDWP attach workflow while retaining Android Studio fallback.

## What Changes

- Add a VS Code debug preparation and attach workflow for Android when a compatible local extension/tooling path is available.
- Add `.vscode/launch.json` only after the workflow is validated.
- Extend the VS Code helper plugin or tasks to prepare the debug APK and surface attach instructions.
- Keep Android Studio fallback documented for environments where VS Code Android attach is unavailable.

## Impact

- Affects `.vscode`, IDE workflow docs, and the VS Code helper extension.
- Does not affect app runtime behavior.
- Requires device/emulator validation evidence for breakpoint attachment.
