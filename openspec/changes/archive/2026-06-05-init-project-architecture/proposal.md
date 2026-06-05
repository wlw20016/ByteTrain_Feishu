# Proposal: init-project-architecture

## 背景

在进入功能实现之前，本项目需要先建立 AI + Bazel + OpenSpec 的工程流程。仓库需要一个稳定的架构骨架，避免 Android、Rust、protobuf、Bazel、AI 上下文和 OpenSpec 工作在模块边界不清的情况下推进。

## 变更内容

- 初始化 app、feature、shared、proto、SDK、docs 和 OpenSpec 顶层目录。
- 增加 Bazel 占位文件，用于标记后续 target 所在位置。
- 增加初始 Kotlin 领域模型和共享分页模型，作为架构锚点。
- 增加消息、邮箱和分页概念的初始 protobuf 契约。
- 增加 Rust SDK 骨架。
- 增加 AI 可读取的项目上下文文档。
- 增加 `openspec/prompt.md`，用于记录 Prompt 证据和人工决策。

## 影响范围

- 本 change 暂不实现生产级 Android 功能。
- 本 change 暂不引入已验证的 Bazel 工具链。
- 后续 change 可以基于当前架构补充实现和验证证据。
