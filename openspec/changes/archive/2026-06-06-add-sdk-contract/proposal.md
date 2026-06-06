# Proposal: add-sdk-contract

## 背景

当前 proto 文件和 Rust SDK 仍是占位骨架。课题要求 Android UI 与 SDK 层之间有稳定契约，Rust SDK 负责 mock 数据、解析、异步服务边界，并使用 protobuf 作为共享数据契约。

## 变更内容

- 对齐 Kotlin 领域模型和 protobuf schema，覆盖消息、邮箱和分页。
- 定义消息和邮箱的分页响应契约。
- 在 Rust SDK 中实现 mock 数据模型、分页逻辑和错误处理。
- 明确异步 SDK 边界和 Kotlin adapter 策略。
- 为后续 UI 数据源从 Kotlin mock 切换到 SDK-backed repository 做准备。

## 影响范围

- 涉及 `proto/`、`sdk/rust/` 和功能模块 data adapter。
- 完整跨语言生成验证可能依赖 `wire-bazel-build` 中的 Bazel/rules 接入。
- UI 渲染行为应保持稳定，只替换数据来源。
