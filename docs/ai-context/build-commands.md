# 构建命令

当前仓库已经有 Bazel 占位文件，但还没有声明 Android、Kotlin、Rust 或 proto 工具链。

## Android UI 临时 Gradle 构建

`add-ui-main-flow` 阶段允许先接入临时 Android+Gradle 构建入口，用于快速验证 App UI 能编译和运行。

规划中的 Gradle 命令：

```bash
./gradlew :app:assembleDebug
```

在 Windows PowerShell 中：

```powershell
.\gradlew :app:assembleDebug
```

Gradle 在本阶段只作为 Android UI 运行验证入口。完整 Bazel rules、targets、query 和 Bazel 构建证据仍由 `wire-bazel-build` change 跟踪。

临时 Gradle 方案约束：

- 先使用单 `:app` 模块。
- `app` 模块可临时通过 sourceSets 纳入 `../shared` 和 `../features`。
- 不改变现有目录结构和包名。
- 后续模块边界稳定后，再拆分 Gradle modules 或迁移 Bazel targets。

## Bazel 规划命令

规划中的命令：

```bash
bazel build //...
bazel test //...
bazel query //...
bazel clean
```

在 Bazel 工具链接入之前，这些命令只是设计目标，还不是已经验证通过的命令。
