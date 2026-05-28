# Proposal: wire-bazel-build

## 背景

当前仓库中的 BUILD 文件只是占位。课题明确要求使用 Bazel 管理模块、依赖、构建、测试、query 分析和工程证据。

## 变更内容

- 选择并记录 Android、Kotlin、proto、Rust 对应的 Bazel rules。
- 逐步接入 proto、shared、feature、app 和 Rust SDK target。
- 将构建、测试和 query 命令沉淀到 AI 上下文文档。
- 记录构建失败、修复过程、query 结果和验证证据。

## 影响范围

- 涉及 `MODULE.bazel`、各层 `BUILD.bazel`、`docs/ai-context`，以及可能的生成代码约定。
- 可能需要本地工具链安装或依赖下载。
- 为后续正式验收建立构建验证路径。
