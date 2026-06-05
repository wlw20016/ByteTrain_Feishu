# 任务：verify-final-bazel-delivery

## 1. 最终验证入口

- [x] BZL-FINAL-001 新增最终 Bazel 验证脚本或文档化流程，运行 app、proto、feature、Rust test 和 query 检查。
- [x] BZL-FINAL-002 确保最终验证入口不依赖 Gradle。
- [x] BZL-FINAL-003 记录 iOS/Xcode 相关命令因本项目为 Android-only 交付而跳过。

## 2. 构建/测试/查询证据

- [x] BZL-FINAL-004 运行并记录 `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4`。
- [x] BZL-FINAL-005 运行并记录 `bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4`。
- [x] BZL-FINAL-006 运行并记录 shared/message/mail Kotlin Bazel 构建目标。
- [x] BZL-FINAL-007 运行并记录 `bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4`。
- [x] BZL-FINAL-008 运行并记录 app 依赖查询摘要。

## 3. 环境闭环

- [x] BZL-FINAL-009 如果任何 Bazel 命令出现 `Access is denied` 等环境输出，在 `docs/ai-context/common-build-errors.md` 中记录根因和修复方式。
- [x] BZL-FINAL-010 环境修复后重新运行被阻塞命令，或保留明确的阻塞记录、负责人和重试命令。

## 4. 文档

- [x] BZL-FINAL-011 更新 `docs/ai-context/build-commands.md`，记录最终验证结果摘要。
- [x] BZL-FINAL-012 如果最终 query 输出与已记录依赖边界不同，更新 `docs/ai-context/module-boundaries.md`。
- [x] BZL-FINAL-013 在标记完成前，更新本 `tasks.md` 并补充命令输出摘要。

## 命令输出摘要

记录日期：2026-06-05。

最终验证入口：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-final-bazel-delivery.ps1
```

入口脚本：`scripts/verify-final-bazel-delivery.ps1`。脚本按 app、proto、shared/feature Kotlin、Rust SDK test、app dependency query 顺序运行 Bazel，失败即停，并写入 `docs/ai-context/final-bazel-delivery-evidence.md`。脚本不调用 Gradle；iOS/Xcode 因 Android-only 交付跳过。

输出摘要：

- App build：`bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` 通过，输出包含 `Target //app:app up-to-date`、`bazel-bin/app/app.apk` 和 `Build completed successfully`。
- Proto build：`bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4` 通过，输出包含 `Target //proto:feed_proto up-to-date` 和 `Build completed successfully`。
- Shared/feature Kotlin build：`bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4` 通过，输出包含 `Found 13 targets` 和 `Build completed successfully`。
- Rust SDK test：`bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4` 通过，输出包含 `//sdk/rust:bytetrain_feed_sdk_test` 和 `PASSED`。
- App query：`bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"` 通过，输出包含 `android_binary rule //app:app`、`kt_android_library rule //app:app_lib`、message/mail feature targets、`//shared/navigation:navigation` 和 `//shared/ui:ui_models`。
- 环境闭环：普通受限入口中 `bazel.cmd`、`bazel.exe` 和 `bazelisk.exe` 曾输出 `Access is denied`/`拒绝访问`。允许执行本机 Bazel 后，阻塞的 app build 已重跑通过，最终脚本完整通过。根因和重试命令已记录在 `docs/ai-context/common-build-errors.md`。
