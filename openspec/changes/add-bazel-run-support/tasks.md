# 任务：add-bazel-run-support

## 1. Run Target 设计

- [x] BZL-RUN-001 决定 `bazel run` 是直接启动 app，还是暴露 Bazel 拥有的 run wrapper。
- [x] BZL-RUN-002 文档化 Android run 验证所需的设备/模拟器前置条件。

## 2. Bazel 实现

- [x] BZL-RUN-003 新增或更新 Bazel run target。
- [x] BZL-RUN-004 确保 run target 依赖 Bazel 构建出的 app artifact，而不是 Gradle output。
- [x] BZL-RUN-005 在适用场景下增加 ADB/device 缺失诊断。

## 3. IDE 集成

- [x] BZL-RUN-006 在 `scripts/ide-build.ps1` 中新增 `run-app` 或等价 target。
- [x] BZL-RUN-007 新增对应 VS Code task。
- [x] BZL-RUN-008 如果插件仍在范围内，新增对应 VS Code helper 插件命令。

## 4. 验证

- [x] BZL-RUN-009 运行 `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4`。
- [x] BZL-RUN-010 运行选定的 `bazel run` 命令，并记录结果或精确设备相关阻塞。
- [x] BZL-RUN-011 运行 IDE helper run target，并记录输出通道行为。

## 5. 文档

- [x] BZL-RUN-012 更新 `docs/ai-context/build-commands.md`，记录 run 命令和证据。
- [x] BZL-RUN-013 更新 `docs/ai-context/ide-bazel-workflow.md`，记录 run 用法。
- [x] BZL-RUN-014 遇到运行专属失败时，更新 `docs/ai-context/common-build-errors.md`。
