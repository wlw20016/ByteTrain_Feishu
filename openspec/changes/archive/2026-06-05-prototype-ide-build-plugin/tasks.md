# 任务：prototype-ide-build-plugin

## 1. 插件原型

- [x] IDE-PLUG-001 创建最小 VS Code 扩展或插件形态原型目录。
  - 证据：新增 `tools/vscode-bazel-helper/package.json`、`tools/vscode-bazel-helper/src/extension.js` 和 `tools/vscode-bazel-helper/README.md`。
- [x] IDE-PLUG-002 新增命令声明，覆盖 app 构建、proto 构建、feature 构建、Rust SDK 测试和 app 依赖查询。
  - 证据：`package.json` 声明 `bytetrain.bazelHelper.buildApp`、`buildProto`、`buildFeatures`、`testRustSdk`、`queryAppDeps`，并同步包含 `assembleDebug` 过渡命令。
- [x] IDE-PLUG-003 通过调用 `scripts/commands/ide-build.ps1 -Target <name>` 实现命令执行。
  - 证据：`src/extension.js` 使用 PowerShell 调用 `scripts/commands/ide-build.ps1 -Target <target>`，target 集合为 `app`、`gradle-app`、`proto`、`features`、`rust`、`query-app-deps`。
- [x] IDE-PLUG-004 在 IDE 可见的输出通道或日志中展示命令输出和非零退出码。
  - 证据：`src/extension.js` 创建 `ByteTrain Bazel Helper` 输出通道，写入 working directory、命令行、stdout/stderr 和 `Exit code`，非零退出码通过 VS Code error message 提示。

## 2. 兼容性

- [x] IDE-PLUG-005 保持 `.vscode/tasks.json` 与插件命令集合一致。
  - 证据：`.vscode/tasks.json` 与插件命令均覆盖 `app`、`gradle-app`、`proto`、`features`、`rust`、`query-app-deps`；`scripts/checks/ide/check-ide-003.ps1` 已静态校验一致性。
- [x] IDE-PLUG-006 记录 Trae 兼容方式，包括 VS Code tasks、扩展命令或直接脚本调用。
  - 证据：`docs/ai-context/build-system/ide-bazel-workflow.md` 新增 `IDE-PLUG 插件原型`，记录 Trae 可复用 VS Code tasks、扩展命令或直接调用 `scripts/commands/ide-build.ps1`。

## 3. 验证

- [x] IDE-PLUG-007 新增聚焦脚本，验证插件 manifest、command IDs 和脚本调用 targets。
  - 证据：新增 `scripts/checks/ide/check-ide-003.ps1`，校验插件 manifest、activationEvents、command IDs、target 映射、`.vscode/tasks.json` target 集合、输出通道关键字和文档记录。
- [x] IDE-PLUG-008 运行聚焦插件校验脚本并记录结果。
  - 证据：`powershell -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-003.ps1` 输出 `IDE-PLUG check passed.`。
- [x] IDE-PLUG-009 至少运行一个轻量 IDE 命令冒烟测试；如果被本地 Bazel 环境阻塞，记录阻塞和重试命令。
  - 证据：`powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target query-app-deps` 非沙箱运行通过，输出包含 `android_binary rule //app:app`、`kt_android_library rule //app:app_lib`、message/mail/shared 依赖标签。沙箱内直接调用 Bazel 出现 `Access is denied.`，重试命令为同一 helper 命令在本机非沙箱环境运行。Bazel 仍打印 Java log handler 警告，但本次 query 退出码为 0。

## 4. 文档

- [x] IDE-PLUG-010 更新 `docs/ai-context/build-system/ide-bazel-workflow.md`，记录插件原型使用方式。
  - 证据：文档新增插件原型目录、命令 ID 到 target 映射、输出通道行为、Trae 兼容方式和聚焦校验脚本。
- [x] IDE-PLUG-011 在标记完成前，更新本 `tasks.md` 并补充证据。
  - 证据：本文件已补充 IDE-PLUG-001 至 IDE-PLUG-010 的完成证据。
