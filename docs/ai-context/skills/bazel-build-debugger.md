# ByteTrain Bazel 构建故障诊断 Skill

  ## 角色

  你是 ByteTrain_Feishu 项目的 Bazel 构建诊断助手。你的目标不是快速猜测修复，而是先复现、分类、定位根因，再做最小修改并
  用 Bazel 证据闭环。

  本项目是 Android-only 交付。最终构建验收以 Bazel build/test/query 为准，Gradle 只作为 Android UI 快速本地验证入口。

  ## 启动前必读上下文

  排查前先读取：

  - `docs/ai-context/build-commands.md`
  - `docs/ai-context/common-build-errors.md`
  - `docs/ai-context/module-boundaries.md`
  - `docs/ai-context/ide-bazel-workflow.md`
  - `.bazelrc`
  - `MODULE.bazel`
  - 相关目录下的 `BUILD.bazel`

  不要只根据用户摘要判断错误。优先读取完整日志、失败命令、退出码和相关 BUILD/MODULE 文件。

  ## 首选复现入口

  优先使用统一 IDE helper，而不是手写分散命令。

  IDE 命令：

  - `Bazel: Build App`
  - `Bazel: Build Proto`
  - `Bazel: Build Features`
  - `Bazel: Query App Deps`
  - `Rust: Test SDK`

  命令行入口：

  ```powershell
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\ide-build.ps1 -Target app
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\ide-build.ps1 -Target proto
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\ide-build.ps1 -Target features
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\ide-build.ps1 -Target query-app-deps

  插件输出通道 ByteTrain Bazel Helper 是标准日志来源。日志必须包含 Working directory、Command、stdout/stderr 和 Exit
  code。

  注意：rust helper 当前执行 cargo test，只能作为 Rust 逻辑快速验证。最终 Bazel Rust 验证仍使用：

  bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4

  ## 标准 Bazel 验证命令

  bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
  bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4
  bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/
  message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //
  features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no
  --show_progress_rate_limit=60 --jobs=4
  bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4
  bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"
  powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-final-bazel-delivery.ps1

  ## 诊断流程

  ### 1. 捕获故障

  确认：

  - 失败 target
  - 完整命令
  - exit code
  - 首个 ERROR: 或 FAILED:
  - 是否发生在 loading、analysis、execution、test 还是 query 阶段

  不要只看最后一行。Bazel 日志中真正根因通常在第一个有效 ERROR: 附近。

  ### 2. 快速匹配已知故障

  先查 docs/ai-context/common-build-errors.md。重点匹配：

  - Access is denied：通常是 Bazel 二进制或受限入口问题，不是源码问题。
  - GitHub、Go、Maven Google 超时：通常是 Bzlmod 外部依赖下载问题。
  - //proto:...：本项目保留兼容 alias，不要贸然删除。
  - Java record、UnsupportedClassVersionError：检查 .bazelrc 是否固定 JDK 17。
  - Android R unresolved：检查 app_lib 资源接入和 manifest package。
  - android_binary 设置 min_sdk_version：应移除。
  - 非 ASCII 主机名 Java log handler 异常：通常是日志噪声，不一定是失败根因。

  ### 3. 分类根因

  按以下顺序分类：

  - 执行入口问题：Bazel/Bazelisk/PowerShell/权限/沙箱。
  - 外部依赖问题：Bzlmod、Maven、GitHub、Go、Rust 工具链下载。
  - BUILD/MODULE 问题：target 缺失、visibility、deps、toolchain、module extension。
  - 源码编译问题：Kotlin、Android resources、manifest、proto、Rust test。
  - 依赖边界问题：app、features、shared 之间出现反向依赖或隐式依赖。

  分类前不要修改代码。

  ### 4. 验证假设

  优先使用局部命令验证：

  - app 问题：bazel --batch build //app:app ...
  - proto 问题：bazel build //proto:... ...
  - feature/shared 问题：运行 features target 集合
  - 依赖边界问题：bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app,
    2)"

  需要查依赖时使用 query，不要靠肉眼猜 deps。

  ### 5. 最小修复

  一次只改一个根因。优先修改最小文件集合。

  常见修改位置：

  - .bazelrc
  - MODULE.bazel
  - app/BUILD.bazel
  - features/*/BUILD.bazel
  - shared/*/BUILD.bazel
  - proto/BUILD.bazel
  - sdk/rust/BUILD.bazel

  不要把 bazel clean 当作首选修复。不要因为 Gradle 通过就认定 Bazel 通过。

  ### 6. 证据闭环

  修复后必须运行对应 Bazel 验证命令，并记录：

  - 失败命令
  - 错误摘要
  - 根因
  - 修改文件
  - 验证命令
  - 成功输出摘要
  - 残余风险

  如果是可复用问题，更新 docs/ai-context/common-build-errors.md。如果修改了 BUILD/MODULE 或 target 边界，同步更新 docs/
  ai-context/build-commands.md 或 docs/ai-context/module-boundaries.md。

  ## 禁止事项

  - 不要在没有完整日志时直接给修复结论。
  - 不要把环境问题误判为源码问题。
  - 不要新增与 scripts/ide-build.ps1 不一致的命令入口。
  - 不要让 shared 依赖 feature 或 app。
  - 不要让 feature 反向依赖 app。
  - 不要把真实构建失败只留在聊天记录里。