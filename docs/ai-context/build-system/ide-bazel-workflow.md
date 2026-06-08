# IDE 与 Bazel 协作工作流

本文档说明团队如何在 Trae / VS Code / Android Studio 中编辑、索引、构建、测试和诊断本项目，并说明 Bazel 与 IDE 的职责边界。

## 角色分工

IDE 是日常工作台，负责：

- 编辑 Kotlin、Rust、proto、BUILD、OpenSpec 和文档文件。
- 使用搜索、跳转、diff、终端和 AI 辅助完成代码改动。
- 在 Android UI 阶段通过 Gradle 或 Android Studio 快速构建、安装和预览 App。
- 展示构建日志、错误位置和 Git 改动。

Bazel 是工程化构建系统，负责：

- 表达 app、feature、shared、proto、Rust SDK 的真实 target 和依赖边界。
- 验证模块是否能被可复现地构建和测试。
- 通过 `bazel query` 分析显式依赖关系。
- 为 OpenSpec、PR 和最终验收提供构建、测试、query 证据。

简化理解：

- IDE 负责“写、看、改、调”。
- Bazel 负责“构建、测试、依赖图、证据”。

## 推荐开发流程

1. 从 `main` 拉取最新代码，并创建功能分支。
2. 阅读对应 OpenSpec change 的 `proposal.md`、`design.md` 和 `tasks.md`。
3. 在 IDE 中修改代码和文档。
4. 如果是 Android UI 改动，先运行 Gradle 快速验证。
5. 如果涉及模块边界、BUILD 文件、proto 或 Rust SDK，再运行对应 Bazel 命令。
6. 如果构建失败，记录失败命令、错误摘要、根因和修复方式。
7. 更新 OpenSpec `tasks.md` 中的任务状态和证据。
8. 提交 commit，推送功能分支并创建 PR。

## Android UI 开发

第一阶段 Android UI 仍允许使用 Gradle 作为快速本地验证入口：

```powershell
.\gradlew.bat :app:assembleDebug
```

Gradle 的职责：

- 验证当前 Android App 能编译和打包 debug APK。
- 支持本地真机或模拟器安装预览。
- 帮助 UI 开发快速发现 Kotlin/Android 编译错误。

Gradle 不负责：

- Bazel target 边界验收。
- Bazel query 依赖图。
- proto、Rust SDK 和全工程 Bazel 构建证据。

预览 App 时可以使用：

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
adb shell am start -n com.bytetrain.feishuclone/.MainActivity
```

## Bazel 构建验证

Bazel 验证应优先使用已经在 `docs/ai-context/build-system/build-commands.md` 中记录过的命令。

常用命令：

```powershell
bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4
bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4
bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
```

使用建议：

- 改 proto 时，先验证 `//proto:...`。
- 改 shared 或 feature Kotlin 代码时，优先验证对应 shared/feature targets。
- 改 App 入口、manifest、资源或 App 依赖时，验证 `//app:app`。
- 不确定影响范围时，先跑局部 target，再扩大到更高层 target。

## Bazel Query 依赖分析

当需要证明模块边界或排查依赖问题时，使用 query。

推荐命令：

```powershell
bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"
```

这个命令用于查看 `//app:app` 两层以内的显式依赖。`--notool_deps` 和 `--noimplicit_deps` 会排除大量工具链依赖，让评审者更容易看到本仓 app、features、shared 的边界。

query 结果应和 `docs/project/module-boundaries.md` 保持一致：

- `//app:app` 只直接依赖 `//app:app_lib`。
- `//app:app_lib` 组合 message、mail、shared targets。
- feature 不反向依赖 app。
- shared 不依赖具体 feature。

## Rust / Proto / BUILD 文件协作

编辑 Rust：

- 在 IDE 中编辑 `sdk/rust`。
- 本地可先运行 `cargo test` 快速验证 Rust 逻辑。
- Bazel 接入验证由 `//sdk/rust:...` targets 跟踪。

编辑 proto：

- 在 IDE 中编辑 `proto/*.proto`。
- 变更后优先运行 `bazel build //proto:...`。
- 如果 proto 影响 Kotlin/Rust 生成代码，应在后续任务中补充对应生成 target 和验证记录。

编辑 BUILD / MODULE：

- BUILD 文件变更必须同步更新 `docs/project/module-boundaries.md` 或 `docs/ai-context/build-system/build-commands.md`。
- 修改 Bazel 规则版本、module extension 或外部依赖时，应记录采用原因、失败日志和修复方式。

## IDE-002 最小构建辅助入口

本阶段不实现完整 IDE 插件，先交付可被 Trae / VS Code 调用的最小命令包装：

```text
scripts/commands/ide-build.ps1
.vscode/tasks.json
```

`scripts/commands/ide-build.ps1` 是稳定的脚本入口，IDE task、终端和后续插件原型都应优先调用它，而不是在多个地方复制 Bazel 命令。支持的目标如下：

| Target | 用途 | 底层命令 |
| --- | --- | --- |
| `app` | 构建 Bazel Android app target | `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` |
| `gradle-app` | 快速构建 Android debug APK | `.\gradlew.bat :app:assembleDebug` |
| `android-jdwp-debug` | 准备 VS Code Android JDWP attach | 构建 debug APK、安装到设备、启动 wait-for-debugger、转发 `localhost:8700` 到 app JDWP PID |
| `proto` | 验证 proto targets | `bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4` |
| `features` | 验证 shared/message/mail Kotlin targets | 已记录的 shared + feature Bazel target 列表 |
| `rust` | 验证 Rust SDK 逻辑 | `cargo test --manifest-path sdk/rust/Cargo.toml` |
| `query-app-deps` | 查看 app 两层显式依赖 | `bazel --batch query ... "deps(//app:app, 2)"` |

命令行用法：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target app
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target gradle-app
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target android-jdwp-debug
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target proto
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target features
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target rust
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target query-app-deps
```

## BZL-RUN IDE run 入口

IDE run 入口和 build、test、query 入口一样，统一使用 helper script：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target run-app
```

该 target 运行：

```powershell
bazel --batch run --curses=no --show_progress_rate_limit=60 //app:run_app
```

VS Code task：

- `Bazel: run app`

VS Code helper command：

- 命令 ID：`bytetrain.bazelHelper.runApp`
- 标题：`Bazel: Run App`
- Target: `run-app`

Run 前置条件：

- 必须安装 Android SDK Platform-Tools。
- `ANDROID_HOME` 必须指向 Android SDK，或 `adb.exe` 必须位于 `PATH`。
- `adb devices` 必须至少显示一个处于 `device` 状态的设备或模拟器。

2026-06-06 验证：

- `scripts/commands/ide-build.ps1 -Target run-app` 在同一输出流中打印 helper banner、工作目录、精确 Bazel 命令、Bazel APK 路径、ADB 路径和 no-device diagnostic lines，随后以 `Bazel Android app run reported a run prerequisite or launch failure.` 失败。
- 新增 `run-app` task 和 VS Code helper command 后，`scripts/checks/ide/check-ide-003.ps1` 通过。

## IDE-FULL run、debug、indexing 与诊断

决策记录：

- Run path：VS Code/Trae app run 使用 `scripts/commands/ide-build.ps1 -Target run-app`，该入口调用 `bazel --batch run --curses=no --show_progress_rate_limit=60 //app:run_app`。
- Debug path：VS Code/Trae breakpoint 使用 `.vscode/launch.json` 中的 `Android: Attach ByteTrain App (JDWP)`，通过 VS Code Java Debugger attach 到 `localhost:8700`。
- Debug preparation：VS Code 暴露 task `VS Code: prepare Android JDWP debug` 和插件命令 `bytetrain.bazelHelper.prepareAndroidJdwpDebug`，它们调用 `scripts/commands/ide-build.ps1 -Target android-jdwp-debug`。
- Android Studio fallback：VS Code Java Debugger 未安装、JDWP attach 失败或需要 Logcat/device selection 等 Android 专用调试工具时，仍使用 Android Studio。
- `.vscode/launch.json` 已提交，配置只包含 Java attach，不写入本机绝对路径。

Indexing 和 code-completion 前置假设：

- `.vscode/extensions.json` 推荐 Kotlin、Rust、Bazel/Starlark、proto、Java 和 Gradle 支持所需 extensions。
- `.vscode/settings.json` 记录 `BUILD`、`BUILD.bazel`、`MODULE.bazel`、`*.bzl`、`*.bazel` 和 `*.proto` 的稳定文件关联。
- Rust Analyzer 指向相对项目文件 `sdk/rust/Cargo.toml`。
- Workspace settings 有意避免 Android SDK、JDK、Cargo 或 Bazel binary 等用户本机绝对路径。
- Kotlin 和 Android completion 仍依赖本地已安装 tooling 与推荐 extensions。Android Studio 仍是最可靠的 Android-specific indexing 和 breakpoint 工具。

已实现插件能力：

| 命令 ID | 能力 |
| --- | --- |
| `bytetrain.bazelHelper.buildApp` | 运行 Bazel app build helper target。 |
| `bytetrain.bazelHelper.runApp` | 运行 Bazel app run helper target，并暴露设备前置条件失败。 |
| `bytetrain.bazelHelper.assembleDebug` | 运行 Gradle debug APK build helper target，用于 Android Studio preview/debug preparation。 |
| `bytetrain.bazelHelper.prepareDebug` | 运行同一个 Gradle debug APK build helper target，作为明确的 Android Studio breakpoint preparation 入口。 |
| `bytetrain.bazelHelper.prepareAndroidJdwpDebug` | 准备 VS Code Android JDWP attach：构建、安装、启动 wait-for-debugger，并转发到 `localhost:8700`。 |
| `bytetrain.bazelHelper.startAndroidJdwpDebug` | 直接通过 VS Code debug API attach 到已经准备好的 `localhost:8700`。 |
| `bytetrain.bazelHelper.buildProto` | 运行 proto Bazel build helper target。 |
| `bytetrain.bazelHelper.buildFeatures` | 运行 shared/feature Kotlin Bazel build helper target。 |
| `bytetrain.bazelHelper.testRustSdk` | 运行 Rust SDK test helper target。 |
| `bytetrain.bazelHelper.queryAppDeps` | 运行 app dependency query helper target。 |
| `bytetrain.bazelHelper.copyDiagnosticContext` | 复制最近一次 helper 命令、工作目录、退出码和最近输出。 |
| `bytetrain.bazelHelper.openBuildCommands` | 打开 `docs/ai-context/build-system/build-commands.md`。 |
| `bytetrain.bazelHelper.openModuleBoundaries` | 打开 `docs/project/module-boundaries.md`。 |
| `bytetrain.bazelHelper.openCommonBuildErrors` | 打开 `docs/ai-context/build-system/common-build-errors.md`。 |

VS Code Android debug 步骤：

- 安装推荐扩展 `redhat.java` 和 `vscjava.vscode-java-debug`。
- 连接真机或启动模拟器，确认 `adb devices` 至少有一个 `device` 状态的设备。
- 在 `MainActivity` 或其他 Kotlin/Java app 代码中设置 breakpoint。
- 在 Run and Debug 中选择 `Android: Attach ByteTrain App (JDWP)`。
- VS Code 会先运行 `VS Code: prepare Android JDWP debug`：构建 debug APK、安装、`am set-debug-app -w com.bytetrain.feishuclone`、启动 `MainActivity`，并把 app JDWP PID 转发到 `localhost:8700`。
- Java Debugger attach 到 `localhost:8700` 后，app 从 wait-for-debugger 状态继续执行，app 代码 breakpoint 可被命中。
- Android Studio fallback 仍保留：当 VS Code Java Debugger 未安装、attach 失败或需要 Logcat/device selection 等 Android 专用调试工具时，使用 Android Studio。
- VS Code/Trae 仍作为编辑、运行 helper tasks、阅读 AI context 和复制 diagnostic context 的工作区。

验证：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-004.ps1
```

2026-06-06 验证结果：

- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-004.ps1` 通过，输出 `IDE-FULL check passed.`
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target query-app-deps` 作为 IDE helper 命令冒烟测试通过。输出包含 `android_binary rule //app:app`、`kt_android_library rule //app:app_lib`、feature targets 和 shared targets。
- `node --check tools\vscode-bazel-helper\src\extension.js` 通过。
- 2026-06-06 时本地未测试 VS Code/Trae breakpoint debug；该限制已由 2026-06-08 的 JDWP attach workflow 补齐。

2026-06-08 VS Code Android debug 评估：

- 采用 `.vscode/launch.json` + VS Code Java Debugger attach 到 Android JDWP 的路径。
- `adb devices` 显示设备 `000002f62f118b6e` 处于 `device` 状态。
- `scripts/commands/ide-build.ps1 -Target android-jdwp-debug` 设备侧 preLaunch 验证通过：Gradle debug build 成功，设备拒绝重装时脚本确认 app 已安装并继续，随后启动 wait-for-debugger app，并转发 `tcp:8700` 到 app JDWP PID。
- 证据记录在 `docs/evidence/vscode-android-debug-workflow.md`。
- 结论：提交 `.vscode/launch.json`，声明 VS Code/Trae Android JDWP attach workflow；Android Studio fallback 仍保留。

`.vscode/tasks.json` 暴露同一组入口：

- `Bazel: build app`
- `Bazel: run app`
- `Gradle: assemble debug`
- `Android Studio: prepare debug`
- `VS Code: prepare Android JDWP debug`
- `Bazel: build proto`
- `Bazel: build features`
- `Rust: test SDK`
- `Bazel: query app deps`

Trae 可以复用 VS Code task 文件；如果插件形态继续演进，插件只需要调用 `scripts/commands/ide-build.ps1 -Target <name>` 并解析标准输出、退出码和错误日志。

## 构建失败记录

遇到真实构建失败时，不只在聊天里描述，需要沉淀到：

```text
docs/ai-context/build-system/common-build-errors.md
```

每条记录至少包含：

- 日期
- 命令
- 环境
- 错误摘要
- 根因
- 修复方式
- 验证命令
- 是否仍有残余风险

如果失败来自 Bazel 外部依赖下载、Android SDK、JDK、Rust 工具链或代理网络，也需要明确记录。

## AI 助手使用上下文

让 AI 协助排查前，优先提供或让 AI 读取：

- `openspec/project.md`
- 当前 change 的 `proposal.md`、`design.md`、`tasks.md`
- `docs/project/project-structure.md`
- `docs/project/module-boundaries.md`
- `docs/ai-context/build-system/build-commands.md`
- `docs/ai-context/build-system/common-build-errors.md`
- 相关 BUILD 文件和失败日志

AI 给出的建议需要人工判断是否采纳。重要建议、人工取舍和最终结果应记录到 OpenSpec 或 PR 描述中。

## 不应做的事

- 不要只因为 IDE 没报红就认为 Bazel 构建通过。
- 不要只改 Gradle 配置而不同步 Bazel target 边界。
- 不要让 feature 反向依赖 app。
- 不要让 shared 依赖具体 feature。
- 不要把真实构建失败只留在聊天记录里。
- 不要在没有记录命令和结果的情况下勾选 OpenSpec 任务。
- 不要把 `bazel build //...` 的大日志直接贴进文档；应记录命令、关键错误、根因和摘要。

## IDE-PLUG 插件原型

本仓库新增最小 VS Code 扩展原型：

```text
tools/vscode-bazel-helper/
```

该原型不复制 Bazel 命令，所有 IDE 命令都调用统一入口：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target <target>
```

扩展命令集合与 `.vscode/tasks.json` 的 target 集合保持一致：

| 命令 ID | IDE 标题 | Target |
| --- | --- | --- |
| `bytetrain.bazelHelper.buildApp` | Bazel: Build App | `app` |
| `bytetrain.bazelHelper.runApp` | Bazel: Run App | `run-app` |
| `bytetrain.bazelHelper.assembleDebug` | Gradle: Assemble Debug | `gradle-app` |
| `bytetrain.bazelHelper.prepareDebug` | Android Studio: Prepare Debug | `gradle-app` |
| `bytetrain.bazelHelper.prepareAndroidJdwpDebug` | VS Code: Prepare Android JDWP Debug | `android-jdwp-debug` |
| `bytetrain.bazelHelper.startAndroidJdwpDebug` | VS Code: Start Android JDWP Debug | `localhost:8700` |
| `bytetrain.bazelHelper.buildProto` | Bazel: Build Proto | `proto` |
| `bytetrain.bazelHelper.buildFeatures` | Bazel: Build Features | `features` |
| `bytetrain.bazelHelper.testRustSdk` | Rust: Test SDK | `rust` |
| `bytetrain.bazelHelper.queryAppDeps` | Bazel: Query App Deps | `query-app-deps` |
| `bytetrain.bazelHelper.copyDiagnosticContext` | Bazel: Copy Diagnostic Context | 工具命令 |
| `bytetrain.bazelHelper.openBuildCommands` | Bazel: Open Build Commands | 工具命令 |
| `bytetrain.bazelHelper.openModuleBoundaries` | Bazel: Open Module Boundaries | 工具命令 |
| `bytetrain.bazelHelper.openCommonBuildErrors` | Bazel: Open Common Build Errors | 工具命令 |

扩展运行时会打开 `ByteTrain Bazel Helper` 输出通道，记录工作目录、实际 PowerShell 命令、stdout/stderr 和 `Exit code`。非零退出码会通过 VS Code 错误提示暴露，详细日志保留在输出通道中。

Trae 兼容方式：

- 优先复用 `.vscode/tasks.json` 中的同名构建、测试和 query tasks。
- 如果 Trae 支持 VS Code 扩展命令，可复用 `tools/vscode-bazel-helper` 中的 `bytetrain.bazelHelper.*` 命令。
- 如果扩展能力不可用，直接在 Trae 终端调用 `scripts/commands/ide-build.ps1 -Target <target>`，target 名称与上表一致。

聚焦校验脚本：

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-003.ps1
```

该脚本验证插件 manifest、command IDs、`.vscode/tasks.json` target 集合、扩展入口对 `scripts/commands/ide-build.ps1` 的调用方式，以及本节文档记录。

## Organized script paths

The current organized helper entry is:

```text
scripts/commands/ide-build.ps1
.vscode/tasks.json
tools/vscode-bazel-helper
```

Focused IDE validation scripts now live under:

```text
scripts/checks/ide/check-ide-002.ps1
scripts/checks/ide/check-ide-003.ps1
scripts/checks/ide/check-ide-004.ps1
```

The plugin commands still call `scripts/commands/ide-build.ps1 -Target <name>`.
