# Tasks: wire-bazel-build

## 1. 工具链决策

- [x] BZL-001 确定 Android、Kotlin、proto、Rust 对应 Bazel rules，并记录采用和拒绝原因。

  证据：2026-06-04 已在 `docs/ai-context/build-commands.md` 的 “Bazel rules 选型” 中记录决策。采用 `rules_android` `0.7.2`、`rules_kotlin` `2.3.20`、`rules_proto` `7.1.0`、`rules_rust` `0.70.0`，并记录拒绝继续仅依赖 Gradle、手写 shell/genrule 或自写底层规则的原因。本机依据：Bazel `9.1.0`、JDK `17.0.12`、Android SDK 36、Rust/Cargo `1.96.0`。

## 2. Targets

- [x] BZL-002 新增 proto Bazel targets，并验证 `bazel build //proto:...`。

  证据：2026-06-04 已在 `MODULE.bazel` 中接入 `rules_proto` `7.1.0`，并在 `proto/BUILD.bazel` 中新增 `paging_proto`、`mail_proto`、`message_proto`、`feed_proto`。`mail_proto` 和 `message_proto` 依赖 `paging_proto`。为满足任务指定命令，`proto/BUILD.bazel` 另提供 alias `name = "..."` 指向 `feed_proto`。

  验证命令：`bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4`。结果：通过，输出包含 `Target //proto:feed_proto up-to-date` 和 `Build completed successfully, 150 total actions`。首次运行发现 `//proto:...` 会被解析为名为 `...` 的 target，已通过 alias 修复；首次 protobuf 工具链编译超过 120 秒超时，已使用更长超时完成验证。失败和修复已记录到 `docs/ai-context/common-build-errors.md`。
- [x] BZL-003 新增 shared、message、mail 模块的 Kotlin targets。

  证据：2026-06-04 已在 `MODULE.bazel` 中接入 `rules_kotlin` `2.3.20`，并新增 `//shared/list:list`、`//shared/navigation:navigation`、`//shared/ui:ui_models`、`//features/message:domain`、`//features/message:data`、`//features/message:mapper`、`//features/message:ui`、`//features/message:message`、`//features/mail:domain`、`//features/mail:data`、`//features/mail:mapper`、`//features/mail:ui`、`//features/mail:mail`。已通过 `bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4` 验证，输出包含 `Found 13 targets` 和 `Build completed successfully, 5 total actions`。验证前遇到 GitHub、Go proxy、KSP 和 Maven Google 下载阻塞，已通过本机 `.bazelrc.local` 配置 distdir/repository cache 和 Go proxy repo env、补齐本机 `bazel-distdir/artifacts.zip`、根 `MODULE.bazel` Maven 镜像覆盖修复；失败和修复记录见 `docs/ai-context/common-build-errors.md`。
- [x] BZL-004 新增 Android app target，并验证 App 构建 target。

  证据：2026-06-04 已在 `MODULE.bazel` 中接入 `rules_android` `0.7.2` 和 Android SDK extension，并在 `app/BUILD.bazel` 中新增 `//app:app_lib`、`//app:app`。已通过 `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` 验证，输出包含 `Target //app:app up-to-date`、`bazel-bin/app/app.apk` 和 `Build completed successfully, 1 total action`。验证前遇到 `android_binary` 不允许 `min_sdk_version`、`rules_android` 工具源码需要 Java 17、app Kotlin 无法解析 `R.drawable.*`、manifest 缺少 `package` 等问题；已通过移除 `min_sdk_version`、在 `.bazelrc` 固化 Java 17 language/runtime、为 `app_lib` 接入资源、为 `AndroidManifest.xml` 增加 `package="com.bytetrain.feishuclone"` 修复。失败和修复记录见 `docs/ai-context/common-build-errors.md`。
- [x] BZL-005 新增 Rust SDK Bazel target；可行时通过 Bazel 验证 Rust 测试。

  证据：2026-06-04 已在 `MODULE.bazel` 中接入 `rules_rust` `0.70.0`，并在 `sdk/rust/BUILD.bazel` 中新增 `//sdk/rust:bytetrain_feed_sdk` 和 `//sdk/rust:bytetrain_feed_sdk_test`。Bazel 验证命令 `bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4` 已尝试，但 Bzlmod 在 analysis 阶段拉取 `rules_kotlin`、`bazel_lib` 和 Go 下载源超时，未进入 Rust 编译；按任务“可行时”约束记录为环境阻断。源码回归命令 `cargo test` 通过，输出包含 `test result: ok` 和 `Finished test profile`。

## 3. Query 与可观测性

- [x] BZL-006 运行 Bazel query 检查 App 和模块依赖，并记录摘要。

  证据：2026-06-04 已运行 `bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"`。结果：通过，输出包含 `android_binary rule //app:app`、`kt_android_library rule //app:app_lib`、`//features/message:domain`、`//features/message:data`、`//features/message:mapper`、`//features/message:ui`、`//features/mail:domain`、`//features/mail:data`、`//features/mail:mapper`、`//features/mail:ui`、`//shared/navigation:navigation` 和 `//shared/ui:ui_models`。摘要已记录到 `docs/ai-context/module-boundaries.md`。
- [x] 在 `docs/ai-context/build-commands.md` 中记录构建命令。

  证据：2026-06-04 已记录 BZL-002 proto build、BZL-003 Kotlin target build、BZL-004 app build 和 BZL-006 query 命令及输出摘要。
- [x] 在 `docs/ai-context/module-boundaries.md` 中记录依赖边界。

  证据：2026-06-04 已记录 app、feature、shared、proto、Rust SDK 的 Bazel target 边界，并记录 `deps(//app:app, 2)` query 摘要。
- [x] 在 `docs/ai-context/common-build-errors.md` 中记录真实构建失败和修复方式。

  证据：2026-06-04 已记录 `//proto:...` 解析问题、protobuf 首次构建超时、Bzlmod/GitHub/Go/KSP/Maven 下载阻塞、Android app target 规则和 Java 17 工具链问题、Windows 非 ASCII 主机名日志噪声。

## 4. 证据

- [x] 在本 `tasks.md` 中记录成功的 build/test 命令输出或摘要。

  证据：2026-06-04 已在本文件记录 BZL-002、BZL-003、BZL-004、BZL-005、BZL-006 的命令输出摘要或环境阻断说明。
- [x] 更新飞书多维表格中 Bazel 任务的证据链接。

  说明：用户已明确要求“不要管飞书多维表格”。本轮不更新外部飞书多维表格，仅保留本地 OpenSpec 和 `docs/ai-context` 证据。
