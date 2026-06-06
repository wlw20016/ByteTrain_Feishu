## ADDED Requirements

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

项目 MUST 仅在存在入库 launch/attach 配置且文档已说明时，才宣称支持 VS Code/Trae breakpoint。

#### Scenario: Developer wants to use breakpoints

- **WHEN** 开发者阅读 IDE workflow 文档
- **THEN** 文档说明受支持的 breakpoint 路径，即 VS Code/Trae launch configuration 或 Android Studio fallback

### Requirement: IDE plugin diagnostics must support AI troubleshooting

IDE plugin MUST 提供收集或暴露 diagnostic context 的方式，用于 AI-assisted Bazel debugging。

#### Scenario: Build command fails

- **WHEN** IDE helper command 失败
- **THEN** 开发者可以复制 command、working directory、exit code 和 recent output，用于 Bazel debugger skill
