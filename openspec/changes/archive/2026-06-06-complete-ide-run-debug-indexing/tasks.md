# 任务：complete-ide-run-debug-indexing

## 1. IDE 能力决策

- [x] IDE-FULL-001 在 Bazel run 支持可用后，决定 VS Code/Trae 支持的运行路径。
- [x] IDE-FULL-002 决定 VS Code breakpoint support 是通过 `launch.json` 实现，还是委托给 Android Studio。
- [x] IDE-FULL-003 文档化 Kotlin、Rust、Bazel 和 proto 的索引/代码补全前置假设。

## 2. VS Code / Trae 配置

- [x] IDE-FULL-004 新增 `.vscode/extensions.json`，记录推荐 extensions。
- [x] IDE-FULL-005 新增 `.vscode/settings.json`，记录语言支持和文件关联所需的稳定项目配置。
- [x] IDE-FULL-006 不新增 `.vscode/launch.json`；当前不宣称支持 VS Code breakpoint/attach，并已文档化 Android Studio fallback。
- [x] IDE-FULL-007 更新 `.vscode/tasks.json`，加入运行和调试准备 tasks。

## 3. Helper Script 与 Plugin

- [x] IDE-FULL-008 仅为实际支持的 workflow 在 `scripts/commands/ide-build.ps1` 中新增 run/install targets。
- [x] IDE-FULL-009 为受支持的运行/诊断 actions 新增 VS Code helper 插件命令。
- [x] IDE-FULL-010 新增 `Copy Diagnostic Context` 能力，收集命令、退出码和最近输出。
- [x] IDE-FULL-011 保持插件命令与 `scripts/commands/ide-build.ps1` 同步。

## 4. 验证

- [x] IDE-FULL-012 新增聚焦 IDE 的验证脚本。
- [x] IDE-FULL-013 运行 IDE 验证脚本并记录输出。
- [x] IDE-FULL-014 至少运行一个 IDE helper 命令冒烟测试。
- [x] IDE-FULL-015 记录本地无法测试 debug 的精确限制和 fallback debug workflow。

## 5. 文档

- [x] IDE-FULL-016 更新 `docs/ai-context/build-system/ide-bazel-workflow.md`。
- [x] IDE-FULL-017 更新 `tools/vscode-bazel-helper/README.md`。
- [x] IDE-FULL-018 更新 AI context，区分已实现插件能力与 IDE-native/delegated 能力。
