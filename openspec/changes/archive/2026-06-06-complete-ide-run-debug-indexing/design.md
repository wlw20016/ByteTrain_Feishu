## Overview

本 change 将当前 IDE integration 从 build helper 推进为文档化、可验证的开发体验。实现应继续保持 script-first 架构：plugin commands 和 VS Code tasks 调用 shared scripts；IDE-specific configuration 则说明 indexing、completion、run 和 debug 的预期工作方式。

## Run Support

Run support 应在 Bazel run change 可用后与其对齐。在此之前，Android run 可以使用现有 Gradle/ADB preview workflow，但必须明确职责区别：

- Bazel run 实现后是最终工程化 evidence。
- Gradle/ADB run 是 UI 开发阶段的 IDE preview workflow。

## Debug Support

优先选项：

- 如果选定 VS Code Android extension 支持，新增 `.vscode/launch.json` 用于 Android attach/debug。
- 如果 VS Code Android debug 不可靠，则文档化 Android Studio 作为 breakpoint/debug 路径，并提供 VS Code tasks 用于 build/install。

除非 launch 或 attach workflow 经过实际测试，否则最终实现不得宣称 VS Code plugin 支持断点。

## Indexing and Code Completion

新增入库推荐配置，覆盖：

- Kotlin/Android language support。
- Rust language support。
- Bazel/Starlark support。
- Proto support。
- 可选 Android tooling。

`extensions.json` 应列出推荐 extension IDs。`settings.json` 只应包含稳定、项目安全的配置。避免用户本机绝对路径。

## Plugin Enhancements

候选 plugin enhancements：

- `Run App`
- `Copy Diagnostic Context`
- `Open Build Commands`
- `Open Module Boundaries`
- `Open Common Build Errors`

插件必须继续通过 `scripts/commands/ide-build.ps1` 执行命令。

## Verification

新增或扩展 focused check script，验证：

- `.vscode/extensions.json` 存在，并包含 Kotlin、Rust、Bazel/Starlark 和 proto 推荐。
- `.vscode/settings.json` 不包含用户本机绝对路径。
- 如果宣称 VS Code debug，则 `.vscode/launch.json` 存在。
- Plugin command IDs 和 targets 与 `scripts/commands/ide-build.ps1` 保持同步。
- 文档清楚说明哪些 IDE 能力已实现，哪些仍委托给 Android Studio 或 IDE extensions。
