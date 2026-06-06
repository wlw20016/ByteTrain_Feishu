## Why

当前仓库已经有最小 VS Code/Trae build helper 插件和 tasks，但课题要求还包括 IDE 运行、索引、断点和代码提示。这些能力目前主要依赖通用 IDE 行为或文档说明，还没有通过入库配置和可验证 evidence 完整闭环。

## What Changes

- 新增 VS Code/Trae workspace 推荐配置，覆盖 Kotlin、Rust、Bazel、proto 和 Android 开发。
- 新增 Android app launch 或 attach debug 的 run/debug 配置。
- 在适用场景下新增 IDE helper/plugin 的 run 和 diagnostic context 命令。
- 新增索引和代码提示文档，说明所需 extensions 和已知限制。
- 新增验证脚本，检查 IDE 配置、插件 command 映射和文档覆盖。

## Capabilities

### New Capabilities

- `ide-run-debug-indexing`：VS Code/Trae 的 IDE run、debug、indexing 和 completion 支持。

### Modified Capabilities

- `ide-build-plugin`：在可行范围内，将现有插件原型从 build/test/query 扩展到 run 和 diagnostics。

## Impact

- `.vscode/extensions.json`
- `.vscode/settings.json`
- `.vscode/launch.json`
- `.vscode/tasks.json`
- `scripts/commands/ide-build.ps1`
- `tools/vscode-bazel-helper/`
- `docs/ai-context/build-system/ide-bazel-workflow.md`
- `scripts/check-ide-*.ps1`
