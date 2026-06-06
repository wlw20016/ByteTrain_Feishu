## Why

当前最终 Bazel evidence 已证明 `bazel build`、`bazel test` 和 `bazel query`，但还没有证明 `bazel run`。课题要求明确提到可以运行 `bazel build`、`bazel run` 和 `bazel test`，因此仓库需要一个明确的 run target，或一个带证据的 run workflow。

## What Changes

- 新增可通过 Bazel run 调用的 target 或 wrapper，用于 Android app artifact 运行流程。
- 新增面向运行验证的 IDE helper target。
- 文档化本 Android-only 仓库中 `bazel run` 与 Gradle/ADB preview 的职责区别。
- 为选定 run path 补充验证证据。
- 更新 AI context，使后续 AI 能将 run failure 和 build failure 分开诊断。

## Capabilities

### New Capabilities

- `bazel-run-support`：Android app 或 run wrapper 的可验证 Bazel run 入口。

### Modified Capabilities

- `bazel-build`：将 Bazel 工程化证据从 build/test/query 扩展到 run。
- `ide-build-plugin`：可选地通过 shared IDE helper 暴露 run target。

## Impact

- `app/BUILD.bazel`
- `scripts/commands/ide-build.ps1`
- `.vscode/tasks.json`
- `tools/vscode-bazel-helper/`
- `docs/ai-context/build-system/build-commands.md`
- `docs/ai-context/build-system/ide-bazel-workflow.md`
- `docs/ai-context/build-system/common-build-errors.md`
