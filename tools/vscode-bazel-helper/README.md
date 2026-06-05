# ByteTrain Bazel Helper

This is a minimal VS Code extension prototype for ByteTrain IDE build commands. It keeps `scripts/ide-build.ps1` as the single build command source and only maps IDE commands to helper targets.

## Commands

| Command ID | Title | Target |
| --- | --- | --- |
| `bytetrain.bazelHelper.buildApp` | Bazel: Build App | `app` |
| `bytetrain.bazelHelper.assembleDebug` | Gradle: Assemble Debug | `gradle-app` |
| `bytetrain.bazelHelper.buildProto` | Bazel: Build Proto | `proto` |
| `bytetrain.bazelHelper.buildFeatures` | Bazel: Build Features | `features` |
| `bytetrain.bazelHelper.testRustSdk` | Rust: Test SDK | `rust` |
| `bytetrain.bazelHelper.queryAppDeps` | Bazel: Query App Deps | `query-app-deps` |

## Local prototype run

Open this folder in VS Code extension development mode and run the extension host. Each command calls:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File <workspace>\scripts\ide-build.ps1 -Target <target>
```

Command output is written to the `ByteTrain Bazel Helper` output channel, including the working directory, command line, stdout/stderr, and exit code.
