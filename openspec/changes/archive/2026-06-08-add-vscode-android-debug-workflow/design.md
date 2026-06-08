# Design: Add VS Code Android Debug Workflow

## Current State

Before this change, the project recommended Kotlin, Rust, Bazel/Starlark, proto, Java, and Gradle extensions. It exposed build/run tasks and a helper extension, while `ide-bazel-workflow.md` explicitly said VS Code breakpoint support was not claimed.

## Approach

- Use VS Code Java Debugger (`vscjava.vscode-java-debug`) plus Android JDWP attach as the checked-in VS Code/Trae debug path.
- Define a debug workflow with two stages:
  - prepare debug build and JDWP forwarding through `scripts/commands/ide-build.ps1 -Target android-jdwp-debug`,
  - attach VS Code Java Debugger to `localhost:8700`.
- Add `.vscode/launch.json` for the validated Java attach configuration.
- Add plugin commands for Android Studio preparation and VS Code JDWP preparation.
- Document device/emulator prerequisites, attach steps, and fallback behavior.
- Capture evidence for the checked-in launch/task/plugin workflow and document the manual breakpoint validation steps.

## Validation

- `node --check tools/vscode-bazel-helper/src/extension.js`.
- IDE check script updated to verify launch/task/plugin consistency.
- Manual debug evidence with device/emulator: app launches, debugger attaches through `localhost:8700`, and a breakpoint is hit.
- If the local VS Code Java Debugger extension is unavailable, Android Studio fallback remains documented and the repository must not claim the local extension was exercised.
