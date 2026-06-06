# Design: wire-bazel-build

## 增量构建策略

Bazel 应从小而稳定的 target 开始接入，再推进到完整 App：

1. Proto target。
2. Shared Kotlin target。
3. Feature Kotlin target。
4. Android App target。
5. Rust SDK target。
6. Query 和依赖边界证据。

## Target 边界

预期 target 分组：

- `//proto:...`
- `//shared/list:...`
- `//shared/navigation:...`
- `//features/message:...`
- `//features/mail:...`
- `//app:app`
- `//sdk/rust:...`

## 文档

每次构建相关变更都应更新或验证：

- `docs/ai-context/build-system/build-commands.md`
- `docs/project/module-boundaries.md`
- `docs/ai-context/build-system/common-build-errors.md`

## Query 证据

在需要证明模块依赖边界或定位构建失败时，使用 `bazel query`、`cquery` 或 `aquery`。

## 风险

Android、Kotlin、Rust、proto 的 Bazel 集成可能耗时较长。该 change 应优先采用增量、可 review 的步骤，避免一次性大规模接入工具链。
