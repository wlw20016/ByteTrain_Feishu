# 构建命令

当前仓库已经有 Bazel 占位文件，但还没有声明 Android、Kotlin、Rust 或 proto 工具链。

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

规划中的命令：

```bash
bazel build //...
bazel test //...
bazel query //...
bazel clean
```

在 Bazel 工具链接入之前，这些命令只是设计目标，还不是已经验证通过的命令。
