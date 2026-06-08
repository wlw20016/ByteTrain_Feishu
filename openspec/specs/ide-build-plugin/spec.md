# ide-build-plugin Specification

## Purpose
TBD - created by archiving change prototype-ide-build-plugin. Update Purpose after archive.
## Requirements
### Requirement: IDE build plugin prototype must invoke the shared build helper

The IDE plugin prototype MUST invoke `scripts/commands/ide-build.ps1` for build, test, and query commands instead of duplicating Bazel command strings.

#### Scenario: User runs app build from IDE plugin

- **WHEN** the user selects the app build command from the plugin prototype
- **THEN** the plugin invokes `scripts/commands/ide-build.ps1 -Target app`

### Requirement: Plugin output must be visible in the IDE

The IDE plugin prototype MUST expose command output and failure exit codes in an IDE-visible output channel or equivalent log.

#### Scenario: Command fails

- **WHEN** a plugin command exits with a non-zero status
- **THEN** the user can see the failed command, exit code, and output summary inside the IDE

### Requirement: Trae compatibility must be documented

The project MUST document how Trae can use the same build entry points as VS Code.

#### Scenario: Trae user runs a build

- **WHEN** a Trae user wants to run a project build
- **THEN** the documentation identifies whether to use VS Code tasks, plugin commands, or direct `scripts/commands/ide-build.ps1` invocation

### Requirement: IDE configuration must recommend language tooling

项目 MUST 提供入库 IDE 推荐配置，覆盖 Kotlin、Rust、Bazel/Starlark 和 proto 的代码导航与代码提示。

#### Scenario: Developer opens the workspace

- **WHEN** 开发者在 VS Code 或 Trae 中打开仓库
- **THEN** workspace 推荐 Kotlin、Rust、Bazel/Starlark 和 proto 编辑所需 extensions 或 tooling

### Requirement: IDE run support must be explicit

项目 MUST 为 Android app 提供明确 IDE run workflow，或清楚文档化受支持 fallback。

#### Scenario: Developer runs the app from IDE workflow

- **WHEN** 开发者按 IDE run 说明操作
- **THEN** workflow 构建 app，并在设备/模拟器上启动，或清楚报告缺失的设备前置条件

### Requirement: IDE debug support must not be overstated

The project MUST only claim VS Code/Trae breakpoint support when a checked-in, validated launch or attach workflow exists. If no validated VS Code/Trae workflow exists, the project MUST document Android Studio fallback as the supported breakpoint path.

#### Scenario: VS Code breakpoint workflow is supported

- Given a checked-in VS Code launch or attach configuration exists
- When a developer follows the documented debug workflow on a connected device or emulator
- Then the app launches or attaches successfully
- And a breakpoint in app code can be hit
- And the validation evidence records the device/emulator and command path used

#### Scenario: VS Code breakpoint workflow is unavailable

- Given VS Code Android attach tooling cannot be validated
- When a developer reads the IDE workflow documentation
- Then the documentation does not claim VS Code breakpoint support
- And Android Studio fallback is documented as the supported breakpoint workflow

### Requirement: IDE plugin diagnostics must support AI troubleshooting

IDE plugin MUST 提供收集或暴露 diagnostic context 的方式，用于 AI-assisted Bazel debugging。

#### Scenario: Build command fails

- **WHEN** IDE helper command 失败
- **THEN** 开发者可以复制 command、working directory、exit code 和 recent output，用于 Bazel debugger skill

