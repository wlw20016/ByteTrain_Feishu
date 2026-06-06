# ByteTrain Bazel Helper

这是 ByteTrain IDE helper commands 的最小 VS Code extension prototype。它保持 `scripts/commands/ide-build.ps1` 作为唯一脚本命令来源，插件只负责把 IDE commands 映射到 helper targets。

## 命令

| 命令 ID | 标题 | 目标 |
| --- | --- | --- |
| `bytetrain.bazelHelper.buildApp` | Bazel: Build App | `app` |
| `bytetrain.bazelHelper.runApp` | Bazel: Run App | `run-app` |
| `bytetrain.bazelHelper.assembleDebug` | Gradle: Assemble Debug | `gradle-app` |
| `bytetrain.bazelHelper.buildProto` | Bazel: Build Proto | `proto` |
| `bytetrain.bazelHelper.buildFeatures` | Bazel: Build Features | `features` |
| `bytetrain.bazelHelper.testRustSdk` | Rust: Test SDK | `rust` |
| `bytetrain.bazelHelper.queryAppDeps` | Bazel: Query App Deps | `query-app-deps` |

工具命令：

| 命令 ID | 标题 | 用途 |
| --- | --- | --- |
| `bytetrain.bazelHelper.copyDiagnosticContext` | Bazel: Copy Diagnostic Context | 复制最近一次 helper 命令、工作目录、退出码和最近输出，用于 AI 辅助排障。 |
| `bytetrain.bazelHelper.openBuildCommands` | Bazel: Open Build Commands | 打开 `docs/ai-context/build-system/build-commands.md`。 |
| `bytetrain.bazelHelper.openModuleBoundaries` | Bazel: Open Module Boundaries | 打开 `docs/project/module-boundaries.md`。 |
| `bytetrain.bazelHelper.openCommonBuildErrors` | Bazel: Open Common Build Errors | 打开 `docs/ai-context/build-system/common-build-errors.md`。 |

## 本地 prototype 运行

在 VS Code extension development mode 中打开该目录并运行 extension host。每个脚本命令都会调用：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File <workspace>\scripts\commands\ide-build.ps1 -Target <target>
```

命令输出会写入 `ByteTrain Bazel Helper` output channel，包含工作目录、命令行、stdout/stderr 和退出码。

## 运行、调试与索引范围

- App run 使用 `scripts/commands/ide-build.ps1 -Target run-app`，该入口调用 Bazel run wrapper。
- 当前 prototype 不宣称支持 VS Code breakpoint/debug。需要 breakpoint debugging 时，先在 VS Code 中准备 app build，再使用 Android Studio。
- Workspace indexing 和 completion 由 `.vscode/extensions.json` 与 `.vscode/settings.json` 配置，覆盖 Kotlin、Rust、Bazel/Starlark、proto、Java 和 Gradle 支持。

## 验证

运行：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-004.ps1
```
