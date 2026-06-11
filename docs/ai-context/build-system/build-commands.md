# 构建命令

当前仓库已经完成 Android、Kotlin、Rust 和 proto 的 Bazel 接入。最终交付验收以 Bazel build/test/query 证据为准；Gradle 只保留为历史过渡和本地开发辅助入口。

## Bazel rules 选型

BZL-001 记录日期：2026-06-04。

本机环境依据：

- Bazel：`bazel 9.1.0`
- JDK：`17.0.12`，`JAVA_HOME` 指向本机 JDK 17 安装目录
- Android SDK：`ANDROID_HOME` 指向本机 Android SDK 安装目录，已安装 `android-36`、`android-36.1`；build-tools 包含 `35.0.0`、`36.0.0`、`36.1.0`、`37.0.0`
- Rust：`rustc 1.96.0`，`cargo 1.96.0`
- 现有 Gradle 入口：Android Gradle Plugin `8.13.2`、Kotlin Android plugin `2.2.21`、`compileSdk=36`、`minSdk=23`、`targetSdk=36`、JVM target 17

后续 Bazel 接入采用 Bzlmod，在 `MODULE.bazel` 中显式 pin 规则版本。2026-06-04 查询 Bazel Central Registry 后，选型如下：

| 领域 | 采用规则 | 采用原因 | 拒绝方案与原因 |
| --- | --- | --- | --- |
| Android | `rules_android` `0.7.2` | BCR 提供的 Android 规则，直接表达 Android app/library、资源、manifest 和 SDK 依赖；与本机 Android SDK 36 及 JDK 17 接入目标匹配。 | 拒绝继续只依赖 Gradle sourceSets：无法给 Bazel query、target 边界和缓存提供证据。拒绝自写 Android Bazel macro 作为底层规则：维护成本高，容易遗漏 manifest、resource 和 SDK 细节。 |
| Kotlin | `rules_kotlin` `2.3.20` | BCR 提供 Kotlin/JVM/Android 编译规则，适合先接入 shared、features Kotlin target，再连接 Android app target。 | 拒绝用 `java_library` 编译 Kotlin：不能处理 Kotlin 编译语义。拒绝仅通过 Gradle 编译 Kotlin：无法满足 Bazel target 和 query 证据要求。 |
| Proto | `rules_proto` `7.1.0` | BCR 当前可用的 proto 规则，适合先建立 `//proto:...` 的跨语言契约 target；后续 Kotlin/Rust 代码生成可在此基础上分层扩展。 | 拒绝在 BUILD 中手写 protoc shell 命令：不可移植、query 不透明。注意 `rules_proto` 上游仓库已归档，因此仅采用稳定基础 proto target，生成代码规则需要在后续任务中谨慎验证。 |
| Rust | `rules_rust` `0.70.0` | BCR 提供 Rust library/test 和 Cargo 集成能力，匹配 `sdk/rust/Cargo.toml` 的 Rust SDK 边界及本机 Rust 1.96.0。 | 拒绝用 `genrule` 调 `cargo build` 作为主要 target：Bazel 依赖图不可见、缓存和测试语义弱。拒绝暂时把 Rust SDK 排除在 Bazel 外：不满足 change 对 `//sdk/rust:...` 的要求。 |

接入顺序保持 OpenSpec 设计：先 proto，再 shared Kotlin，再 feature Kotlin，再 Android app，最后 Rust SDK 和 query 证据。BZL-001 只记录规则决策；实际 `MODULE.bazel` 和 BUILD target wiring 由 BZL-002 至 BZL-005 分步完成。

## Android UI 临时 Gradle 构建

`add-ui-main-flow` 阶段允许先接入临时 Android+Gradle 构建入口，用于快速验证 App UI 能编译和运行。

已验证的 Gradle 命令：

```bash
./gradlew :app:assembleDebug
```

在 Windows PowerShell 中：

```powershell
.\gradlew.bat :app:assembleDebug
```

BUILD-001 验证记录：

- 日期：2026-06-02
- 分支：`feature/build-001-gradle-entry`
- 命令：`.\gradlew.bat :app:assembleDebug`
- 结果：通过，输出包含 `BUILD SUCCESSFUL`。
- 说明：本次新增的是单 `:app` Gradle 模块入口，`app/build.gradle.kts` 临时将 `../shared` 和 `../features` 纳入 main source set，用于第一阶段 Android UI 编译验证。

Gradle 在本阶段只作为 Android UI 运行验证入口。完整 Bazel rules、targets、query 和 Bazel 构建证据仍由 `wire-bazel-build` change 跟踪。

临时 Gradle 方案约束：

- 先使用单 `:app` 模块。
- `app` 模块可临时通过 sourceSets 纳入 `../shared` 和 `../features`。
- 不改变现有目录结构和包名。
- 后续模块边界稳定后，再拆分 Gradle modules 或迁移 Bazel targets。

## Gradle 到 Bazel 的过渡边界

当前阶段的 Gradle 职责：

- 只用于第一阶段 Android UI 本地编译、打包和运行验证。
- 只承诺 `:app:assembleDebug` 入口可用。
- 临时通过单 `:app` 模块编译 `app/`、`shared/` 和 `features/` 当前 Kotlin 源码。
- 不负责 proto、Rust SDK、Bazel query、Bazel cache 或完整工程依赖分析。

后续 Bazel 职责仍由 `wire-bazel-build` change 跟踪：

- 选择 Android、Kotlin、proto、Rust 对应 Bazel rules。
- 为 `proto/`、`shared/`、`features/message/`、`features/mail/`、`app/` 和 `sdk/rust/` 建立 Bazel targets。
- 验证 `bazel build`、`bazel test`、`bazel query` 等命令，并记录失败、修复和 query 证据。
- 将 Gradle 中临时合并的 sourceSets 拆回清晰的 Bazel 依赖边界。

迁移时的边界原则：

- Gradle sourceSets 是临时运行入口，不代表最终模块依赖形态。
- `shared` 不能依赖具体 feature 或 `app`。
- `features/message` 和 `features/mail` 不能反向依赖 `app`。
- `app` 只负责入口、导航和组合各 feature。
- Bazel 迁移应优先从 `proto/`、`shared/`、feature 模块开始，最后接入 Android app target。

## Bazel 规划命令

已验证的 Bazel 命令：

```powershell
bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4
bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4
bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"
```

## Remote cache 策略

2026-06-11 约定：本地开发/操作机只读取共享 remote cache，不上传本地产物；CI 作为唯一写入方负责上传验证后的构建和测试结果。

本地机器配置放在不提交的 `.bazelrc.local` 中，推荐保持：

```bazelrc
build --remote_cache=http://159.75.170.170:9090
test --remote_cache=http://159.75.170.170:9090
build --remote_upload_local_results=false
test --remote_upload_local_results=false
```

CI 使用已提交的 `.bazelrc.ci` 写入远端缓存：

```powershell
bazel --bazelrc=.bazelrc --bazelrc=.bazelrc.ci build //app:app --curses=no --show_progress_rate_limit=60
bazel --bazelrc=.bazelrc --bazelrc=.bazelrc.ci test //... --curses=no --show_progress_rate_limit=60
```

若 CI 所在机器还需要本机 SDK、镜像或 shell 路径配置，应额外提供 CI 环境专用 bazelrc 或命令行参数；不要复用开发机的 `.bazelrc.local`。

## 最终 Bazel 交付验证

BZL-FINAL 记录日期：2026-06-05。

最终验证入口：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\verify-final-bazel-delivery.ps1
```

该入口只运行 Bazel 命令，不调用 Gradle。iOS、UIKit、AutoLayout 和 Xcode 命令跳过，原因是本仓库为 Android-only 交付。

完整 evidence 文件：

- `docs/evidence/final-bazel-delivery-evidence.md`

最终验证结果摘要：

| 检查 | 命令 | 结果 |
| --- | --- | --- |
| Android app 构建 | `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` | 通过；输出包含 `Target //app:app up-to-date` 和 `Build completed successfully, 1 total action`。 |
| Proto 构建 | `bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4` | 通过；输出包含 `Target //proto:feed_proto up-to-date` 和 `Build completed successfully, 1 total action`。 |
| Shared/message/mail Kotlin build | `bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4` | 通过；输出包含 `Found 13 targets` 和 `Build completed successfully, 1 total action`。 |
| Rust SDK Bazel 测试 | `bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4` | 通过；输出包含 `//sdk/rust:bytetrain_feed_sdk_test (cached) PASSED` 和 `Build completed successfully, 1 total action`。 |
| App dependency query | `bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"` | 通过；输出包含 app、message/mail feature 和 shared 显式依赖边界。 |

环境闭环：

- 在普通沙箱内直接执行 `bazel`/`bazel.exe`/`bazelisk` 时曾出现 `Access is denied`/`拒绝访问`，属于本机 Bazel 二进制执行权限或沙箱入口问题，不是源码构建失败。
- 通过允许执行本机 Bazel 后，被阻塞的 app build 已重新运行并通过；最终脚本也完整运行通过。
- 详细根因、修复方式和重试命令记录在 `docs/ai-context/build-system/common-build-errors.md`。

AI-ARCH 审计：

- 2026-06-05 复核：本文档已覆盖最终 Android-only Bazel 验收入口、结果摘要、Gradle 非最终验收边界和 iOS/Xcode 跳过原因。

BZL-002 验证记录：

- 日期：2026-06-04
- 命令：`bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4`
- 结果：通过，输出包含 `Target //proto:feed_proto up-to-date` 和 `Build completed successfully, 150 total actions`。
- 说明：`//proto:...` 是本任务指定命令。`proto/BUILD.bazel` 中同时保留正常聚合 target `feed_proto`，并提供兼容 alias `name = "..."` 让该命令可作为 `feed_proto` 的入口。

BZL-003/BZL-004/BZL-005 接入记录：

- 日期：2026-06-04
- Kotlin targets：已新增 `//shared/list:list`、`//shared/navigation:navigation`、`//shared/ui:ui_models`、`//features/message:domain`、`//features/message:data`、`//features/message:mapper`、`//features/message:ui`、`//features/message:message`、`//features/mail:domain`、`//features/mail:data`、`//features/mail:mapper`、`//features/mail:ui`、`//features/mail:mail`。
- Android targets：已新增 `//app:app_lib` 和 `//app:app`。
- Rust targets：已新增 `//sdk/rust:bytetrain_feed_sdk` 和 `//sdk/rust:bytetrain_feed_sdk_test`。
- Bazel 验证状态：BZL-003 已通过 `bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4` 验证，输出包含 `Found 13 targets` 和 `Build completed successfully, 5 total actions`。BZL-004 已通过 `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` 验证，输出包含 `Target //app:app up-to-date`、`bazel-bin/app/app.apk` 和 `Build completed successfully, 1 total action`。`bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4` 仍需单独复验；此前外部依赖下载失败和修复详见 `docs/ai-context/build-system/common-build-errors.md`。
- 源码回归验证：`.\gradlew.bat :app:assembleDebug` 通过，输出包含 `BUILD SUCCESSFUL in 18s`；`cargo test` 通过，输出包含 `test result: ok` 和 `Finished test profile`。

BZL-006 query 记录：

- 日期：2026-06-04
- 命令：`bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"`
- 结果：通过，输出包含 `android_binary rule //app:app`、`kt_android_library rule //app:app_lib`、message/mail feature targets、`//shared/navigation:navigation` 和 `//shared/ui:ui_models`。
- 说明：query 使用 `--notool_deps --noimplicit_deps` 排除 Android/Bazel 工具链隐式依赖，聚焦本仓 app、feature、shared 显式边界。更大的 `deps(//app:app)` 查询会包含大量外部 tool deps，不适合直接作为人工评审摘要。

## Bazel run 支持

BZL-RUN 记录日期：2026-06-06。

运行策略：

- `//app:run_app` 是由 `app/BUILD.bazel` 生成的 Bazel-owned run wrapper。
- 该 wrapper 依赖 `//app:app`，并使用生成 runner 旁边的 Bazel-built APK：`bazel-bin/app/app.apk`。
- 该 wrapper 不读取 Gradle outputs。
- Android run 验证要求已安装 Android SDK Platform-Tools，`ANDROID_HOME` 可用或 `adb.exe` 位于 `PATH`，并且 `adb devices` 能看到在线设备或模拟器。

已验证命令：

```powershell
bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
bazel --batch run --curses=no --show_progress_rate_limit=60 //app:run_app
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target run-app
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-003.ps1
```

验证结果：

| 检查 | 结果 |
| --- | --- |
| App 构建 | 通过。输出包含 `Target //app:app up-to-date`、`bazel-bin/app/app.apk` 和 `Build completed successfully, 1 total action`。 |
| Bazel run target | 已进入 wrapper 的 no-device diagnostic path。输出包含 Bazel APK 路径、`D:\Android\AndroidSDK\platform-tools\adb.exe`、`[run-app] No online Android device or emulator was found.`，以及没有设备条目的 `List of devices attached`。 |
| IDE helper run target | 通过本环境的 output channel 行为验证：`scripts/commands/ide-build.ps1 -Target run-app` 输出 helper banner、工作目录、精确 Bazel 命令和相同 no-device diagnostic lines，随后以 `Bazel Android app run reported a run prerequisite or launch failure.` 失败。 |
| VS Code helper 一致性 | 通过。新增 `run-app` 命令和 task 后，`scripts/checks/ide/check-ide-003.ps1` 输出 `IDE-PLUG check passed.`。 |

环境说明：

- 在受限 shell 中直接运行 `bazel` 仍会在 Bazel analysis 前输出 `Access is denied.`。上述验证已通过获准的本机 Bazel 执行路径重跑，和既有 BZL-FINAL-009 环境说明一致。
- 2026-06-06 本机没有连接设备或模拟器，因此未尝试 install 和 activity launch。这是设备前置条件阻塞，不是 APK build failure。

## 全仓 Bazel 验证

BZL-FULL 记录日期：2026-06-08。

验收入口：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\verify-full-bazel-workspace.ps1
```

验收目标集合：

- Build：`//...`
- Test：`//...`
- 允许排除：无
- Query 摘要：`bazel --batch query --output=label_kind --curses=no "kind('.* rule', //...)"`

完整 evidence 文件：

- `docs/evidence/full-bazel-workspace-evidence.md`

本次执行结果：

| 检查 | 命令 | 结果 |
| --- | --- | --- |
| Full workspace build | `bazel --batch build //... --curses=no --show_progress_rate_limit=60 --jobs=4` | 通过；输出包含 `Analyzed 25 targets`、`Found 25 targets` 和 `Build completed successfully, 1 total action`。 |
| Full workspace test | `bazel --batch test //... --curses=no --show_progress_rate_limit=60 --jobs=4` | 通过；输出包含 `Found 24 targets and 1 test target`、`//sdk/rust:bytetrain_feed_sdk_test (cached) PASSED` 和 `Executed 0 out of 1 test: 1 test passes`。 |
| Full workspace query | `bazel --batch query --output=label_kind --curses=no "kind('.* rule', //...)"` | 通过；输出包含 `Rule targets: 25`，覆盖 app、features、proto、Rust SDK、shared targets。 |

结论：

- `//...` 是当前全仓验收集合，没有记录任何 target 排除。
- 2026-06-08 允许当前执行环境调用本机 Bazel 后，`bazel --batch build //...` 和 `bazel --batch test //...` 均通过。
- Windows PowerShell/Bazel shim 会剥掉 query 表达式中的转义双引号；脚本改用 `kind('.* rule', //...)` 后 query 摘要通过。
- Bazel 9.1.0 仍会输出非 ASCII 主机名相关 Java log handler 噪声；该噪声未影响 build/test/query 退出码。

## Rust async/protobuf SDK 验证

`complete-rust-sdk-async-protobuf` 没有新增 Bazel target 或外部 Rust crate，`sdk/rust/BUILD.bazel` 继续暴露：

- `//sdk/rust:bytetrain_feed_sdk`
- `//sdk/rust:bytetrain_feed_sdk_test`

已验证命令：

```powershell
cargo test --manifest-path sdk/rust/Cargo.toml
bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4
```

`cargo test` 结果：14 个 Rust 单测通过，doc-tests 0 个。Bazel 结果记录在本 change 的 tasks 中。

规划中的命令：

```bash
bazel build //...
bazel test //...
bazel query //...
bazel clean
```

除上方明确记录的 proto 构建命令外，这些命令仍是设计目标，还不是已经验证通过的命令。


