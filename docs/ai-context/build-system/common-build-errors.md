# 常见构建错误

本文档用于沉淀构建失败、根因分析和已验证的修复方式。

## 记录模板

每条构建失败记录使用下面的结构：

### 错误

命令：

    bazel build //...

输出摘要：

    ...

根因：

修复方式：

验证结果：

### BZL-002: `//proto:...` 被解析为普通 target

命令：

    bazel build //proto:...

输出摘要：

    ERROR: Skipping '//proto:...': no such target '//proto:...': target '...' not declared in package 'proto'

根因：

`//proto:...` 在 Bazel 中不是递归通配符写法，而是包 `//proto` 下名为 `...` 的 target。标准递归通配符应写作 `//proto/...`，但 BZL-002 任务指定了 `bazel build //proto:...`。

修复方式：

在 `proto/BUILD.bazel` 中新增正常聚合 target `feed_proto`，并增加兼容 alias：

    alias(
        name = "...",
        actual = ":feed_proto",
    )

验证结果：

`bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4` 通过，输出包含 `Target //proto:feed_proto up-to-date` 和 `Build completed successfully, 150 total actions`。

### BZL-002: 首次构建 protobuf 工具链超时

命令：

    bazel build //proto:...

输出摘要：

    command timed out after 123952 milliseconds
    ERROR: build interrupted

根因：

首次接入 `rules_proto` 时，Bazel 需要下载并编译 protobuf、abseil、upb 等工具链依赖。Windows 本机首次 C++ 编译耗时超过 120 秒工具超时限制。输出中还出现 Visual Studio `/showIncludes` 语言识别警告，以及 abseil `C4819` 编码警告，但它们不是最终阻断原因。

修复方式：

使用更长超时重新运行，并降低进度输出频率、限制并发：

    bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4

验证结果：

命令在 157 秒左右完成，输出包含 `Build completed successfully, 150 total actions`。

### BZL-003/BZL-004/BZL-005: Bzlmod 下载 Kotlin/Rust/Android 依赖超时

命令：

    bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models --curses=no --show_progress_rate_limit=60 --jobs=4
    bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
    bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4

输出摘要：

    WARNING: Download from https://github.com/bazelbuild/rules_kotlin/releases/download/v2.3.20/rules_kotlin-v2.3.20.tar.gz failed: class java.io.IOException Connect timed out
    WARNING: Download from https://github.com/bazel-contrib/bazel-lib/releases/download/v3.1.0/bazel-lib-v3.1.0.tar.gz failed: class java.io.IOException Connect timed out
    WARNING: Download from https://go.dev/dl/?mode=json&include=all failed: class java.io.IOException Connect timed out
    ERROR: Analysis of target '//sdk/rust:bytetrain_feed_sdk_test' failed; build aborted

根因：

Bzlmod 解析 `rules_kotlin`、`rules_rust` 及其传递依赖时需要访问 GitHub 和 Go 下载源。本机当前网络到这些源超时，导致 Bazel 在 analysis 阶段失败或长时间等待。该失败发生在外部仓库下载阶段，早于 Kotlin、Android 或 Rust target 的源码编译。

修复方式：

已在本机 `.bazelrc.local` 中配置 `--distdir`、`--repository_cache`、`GOPROXY=https://goproxy.cn,https://proxy.golang.org,direct` 和 `GOSUMDB=sum.golang.google.cn`。本机 `bazel-distdir` 已预热 `rules_kotlin-v2.3.20.tar.gz`、`bazel-lib-v3.1.0.tar.gz`，并补齐 `https://github.com/google/ksp/releases/download/2.3.6/artifacts.zip`；该 KSP zip 的 SHA256 为 `685d895c746df2e2159174a9fac9dcc029c3e127612c677bbc5502dd71e98ffe`，与 `rules_kotlin` 声明一致。

复验过程中 Maven Google 继续超时，报错示例：

    Error downloading com.android.tools.analytics-library:shared:30.1.3
    download error: Caught java.net.ConnectException while downloading https://maven.google.com/com/android/tools/analytics-library/shared/30.1.3/shared-30.1.3.pom

根因是 `rules_android` 通过 `rules_jvm_external` 的 `android_ide_common_30_1_3` 和 `rules_android_maven` Maven install 拉取 Android 工具依赖，默认仓库包含 `https://maven.google.com`。本机到该源超时。已在根 `MODULE.bazel` 中添加直接 `rules_jvm_external` `6.9` 依赖，并为同名 Maven install 覆盖 repositories，优先使用 `https://maven.aliyun.com/repository/google` 和 `https://maven.aliyun.com/repository/central`，保留官方源作为回退。根模块覆盖时必须保留 `aar_import_bzl_label = "@rules_android//rules:rules.bzl"` 和 `use_starlark_android_rules = True`，否则会生成缺失 `aar_import` 的 Maven repo。

验证结果：

2026-06-04 复验 BZL-003 完整 Kotlin target 集合通过：

    bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4

输出包含 `Found 13 targets` 和 `Build completed successfully, 5 total actions`。源码层回归命令此前已通过：`.\gradlew.bat :app:assembleDebug` 输出 `BUILD SUCCESSFUL in 18s`，`cargo test` 输出 `test result: ok`。BZL-004 App target 和 BZL-005 Rust Bazel test 仍需按各自任务单独复验。

### BZL-004: Android app target 规则和工具链配置错误

命令：

    bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4

输出摘要：

    Target @@//app:app is not allowed to set a min_sdk_version value.
    error: could not locate class file for java.lang.Record
    UnsupportedClassVersionError: AddJarManifestEntry has been compiled by a more recent version of the Java Runtime
    error: unresolved reference 'R'
    error: <manifest> must have a 'package' attribute

根因：

`rules_android` `android_binary` 不允许在该 target 上设置 `min_sdk_version`。`rules_android` 自身工具源码使用 Java `record`，且 `rules_jvm_external` 工具按 Java 17 class file 编译，因此 Bazel Java language/runtime 需要统一到 17。`MainActivity` 引用 `R.drawable.*`，但 `//app:app_lib` 起初没有接入 `src/main/res`，Kotlin 编译阶段看不到资源生成的 `R`。Gradle 可通过 `namespace` 补齐 manifest package，但 Bazel/aapt2 需要 `AndroidManifest.xml` 显式包含 `package`。

修复方式：

从 `app/BUILD.bazel` 的 `android_binary(name = "app")` 中移除 `min_sdk_version`。在 `.bazelrc` 固化 `--java_language_version=17`、`--tool_java_language_version=17`、`--java_runtime_version=remotejdk_17`、`--tool_java_runtime_version=remotejdk_17`。在 `//app:app_lib` 上增加 `resource_files = glob(["src/main/res/**"])`，并在 `app/src/main/AndroidManifest.xml` 增加 `package="com.bytetrain.feishuclone"`。

验证结果：

2026-06-04 复验通过：

    bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4

输出包含 `Target //app:app up-to-date`、`bazel-bin/app/app.apk` 和 `Build completed successfully, 1 total action`。

### BZL-003/BZL-004/BZL-005: Windows 非 ASCII 主机名污染 Bazel Java 日志

命令：

    bazel --batch build //shared/list:list

输出摘要：

    Can't load log handler "com.google.devtools.build.lib.util.SimpleLogHandler"
    java.lang.IllegalArgumentException: Expected internal string with Latin-1 coder
    java.log.<non-ascii-host>.<user>.log.java

根因：

Bazel Java 日志路径包含本机非 ASCII 主机名片段，Bazel 9.1.0 的日志 handler 在 Windows 上尝试按 Latin-1 内部字符串处理时打印异常。

修复方式：

该问题当前只污染日志输出，不是构建失败的直接原因。若后续需要消除噪声，可在本机 `.bazelrc.local` 配置 ASCII-only output user root，或调整主机名/日志路径。

验证结果：

后续失败仍指向外部依赖下载超时；该日志异常未单独中断命令。

### BZL-FINAL-009: Bazel 二进制在受限入口中拒绝访问

命令：

    bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
    bazel.exe version
    bazelisk version

输出摘要：

    bazel.cmd : Access is denied.
    程序“bazel.exe”无法运行: 拒绝访问。
    程序“bazelisk.exe”无法运行: 拒绝访问。

根因：

PowerShell 中的 `bazel` 解析到 `C:\Users\23064\bin\bazel.cmd`，该 shim 再调用 WinGet 安装目录下的 `bazel.exe`。在受限执行入口中，用户目录/WinGet 下的 Bazel 或 Bazelisk 二进制被拒绝执行，导致命令在 target analysis 前失败。该问题发生在本机执行权限或沙箱边界，不是 `//app:app`、proto、feature 或 Rust target 的源码错误。

修复方式：

在受信任的本机 shell 中运行 Bazel，或在需要沙箱审批的执行环境中允许 `bazel` 命令执行。若脱离审批环境后仍出现拒绝访问，需要修复 WinGet 安装目录下 `bazel.exe`/`bazelisk.exe` 的 ACL、重新安装 Bazel/Bazelisk，或将可执行的 Bazel 二进制路径放到 PATH 前置位置。

重试命令：

    bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\verify-final-bazel-delivery.ps1

验证结果：

2026-06-05 允许执行本机 Bazel 后，`bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` 通过，输出包含 `Target //app:app up-to-date`、`bazel-bin/app/app.apk` 和 `Build completed successfully, 26 total actions`。随后最终入口脚本完整通过，生成 `docs/evidence/final-bazel-delivery-evidence.md`，最终 app build 摘要包含 `Build completed successfully, 1 total action`。

### BZL-RUN：没有在线 Android 设备或模拟器

命令：

    bazel --batch run --curses=no --show_progress_rate_limit=60 //app:run_app
    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target run-app

输出摘要：

    Bazel run wrapper: Android app
    APK: "<bazel execroot>\bazel-out\x64_windows-fastbuild\bin\app\app.apk"
    ADB: "D:\Android\AndroidSDK\platform-tools\adb.exe"
    [run-app] No online Android device or emulator was found.
    [run-app] Start an emulator or connect a device with USB debugging enabled, then verify `adb devices` shows a device state.
    List of devices attached

根因：

Bazel-built APK 和 ADB 均可用，但 `adb devices` 没有返回处于 `device` 状态的设备。这是 run 环境前置条件阻塞，不是 Bazel APK build failure，也不是 Gradle output 问题。

修复方式：

启动 Android emulator，或连接已开启 USB debugging 的物理设备。先验证：

    adb devices

该命令必须至少显示一个处于 `device` 状态的设备或模拟器，然后再重跑 `//app:run_app`。

验证结果：

2026-06-06 重跑后进入预期 no-device diagnostic path。IDE helper 在同一输出流中打印相同诊断行，并以 `Bazel Android app run reported a run prerequisite or launch failure.` 失败。由于没有在线设备，未尝试 install 和 activity launch。

### BZL-FULL: 全仓 Bazel 验证入口和 query 引号问题

命令：

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\verify-full-bazel-workspace.ps1
    bazel --batch build //... --curses=no --show_progress_rate_limit=60 --jobs=4
    bazel --batch test //... --curses=no --show_progress_rate_limit=60 --jobs=4
    bazel --batch query --output=label_kind --curses=no "kind('.* rule', //...)"

输出摘要：

    Access is denied.
    Rule targets: 0
    ERROR: Error while parsing 'kind(.* rule, //...)': syntax error at 'rule , //...'

根因：

初次在受限执行入口运行时，当前环境不能调用 PowerShell 中解析到的本机 `bazel` shim/二进制，失败发生在 Bazel target analysis 前，和 `//...` 下具体 target 无关。允许执行本机 Bazel 后，build/test 通过，但 query 表达式 `kind(\".* rule\", //...)` 经 Windows PowerShell/Bazel shim 传递后被剥掉双引号，Bazel 实际收到 `kind(.* rule, //...)` 并报语法错误。query 未返回任何 rule targets 时，脚本按失败处理，避免把入口权限或 query 表达式失败误判为全仓通过。

修复方式：

在受信任的本机 shell 中运行同一个验证入口，或允许当前执行环境执行本机 Bazel。若脱离受限入口后仍输出 `Access is denied.`，按 BZL-FINAL-009 的路径修复 Bazel/Bazelisk 安装目录 ACL、重新安装 Bazel/Bazelisk，或将可执行 Bazel 二进制路径放到 PATH 前置位置。query 表达式改为 `kind('.* rule', //...)`，避免 Windows shim 剥掉字符串引号。

重试命令：

    powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\commands\verify-full-bazel-workspace.ps1

验证结果：

2026-06-08 允许当前执行环境调用本机 Bazel 并修正 query 表达式后，全仓验证脚本通过。`docs/evidence/full-bazel-workspace-evidence.md` 记录：`bazel --batch build //...` 通过，输出包含 `Analyzed 25 targets`、`Found 25 targets` 和 `Build completed successfully, 1 total action`；`bazel --batch test //...` 通过，输出包含 `Found 24 targets and 1 test target`、`//sdk/rust:bytetrain_feed_sdk_test (cached) PASSED` 和 `Executed 0 out of 1 test: 1 test passes`；query 通过并返回 `Rule targets: 25`。

## AI-ARCH 审计

2026-06-05 复核：本文档已覆盖最终 Bazel 验证前后仍需要保留的构建失败和环境阻塞，包括 proto target 兼容、首次工具链超时、Bzlmod 外部依赖下载、Android app target 配置、非 ASCII 主机名日志噪声和受限入口 `Access is denied`。当前无未记录的最终验证阻塞。
