## Overview

本 change 引入可验证的 Bazel run 路径。Android app 运行涉及设备/模拟器前置条件，因此实现时应区分 artifact 产出和设备启动，并明确所有设备要求。

## Run Strategy

优先策略：

- 新增 `bazel run` target，使用 Bazel-owned outputs 产出或启动 Android app artifact。
- 如果 Windows 上无法可靠通过 Bazel 直接启动 Android app，则新增一个入库 wrapper target：打印 APK 路径，并在设备可用时可选调用 ADB。

可接受 fallback：

- 文档化 Bazel artifact run workflow：`bazel build //app:app` 产出 `bazel-bin/app/app.apk`，随后显式执行 ADB install/start。
- 只有存在一个 `bazel run` target 来验证或暴露该 run workflow 时，这个 fallback 才算满足本 change。

## IDE Integration

只有在 Bazel run 行为定义清楚后，`scripts/commands/ide-build.ps1` 才应新增类似 `run-app` 的运行 target。VS Code plugin 和 `.vscode/tasks.json` 应继续调用该脚本，而不是复制命令。

## Diagnostics

Run failure 必须单独分类：

- APK 产出前的 Bazel build failure。
- Android 设备或模拟器缺失。
- ADB install failure。
- Activity launch failure。
- Windows 权限或 sandbox 问题。

可复用失败应记录到 `docs/ai-context/build-system/common-build-errors.md`。

## Verification

最小验证应包含：

```powershell
bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
bazel run <selected-run-target> --curses=no --show_progress_rate_limit=60
```

如果没有设备/模拟器，必须记录精确设备前置条件，并验证 run target 能进入预期 no-device diagnostic path，且错误信息不误导。
