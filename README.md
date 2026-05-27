# ByteTrain 仿飞书 Android

本仓库用于初始化一个由 AI、Bazel 和 OpenSpec 驱动的 Android 工程实践项目。

## 项目范围

- 使用 Kotlin 开发包含消息和邮箱两个 Tab 的 Android App。
- 建立可复用的分页列表、导航和 UI 状态抽象。
- 使用 Rust SDK 承担 mock 数据解析和异步服务边界。
- 使用 Protobuf 定义 Kotlin 与 Rust 共享的数据契约。
- 按 Bazel 思路规划模块边界和构建目标。
- 使用 OpenSpec 沉淀需求、设计、任务、验收和 AI 协作证据。

## 目录说明

```text
app/                    Android App 入口
features/message/       消息 Tab 功能
features/mail/          邮箱 Tab 功能
shared/                 通用 UI、列表和导航抽象
proto/                  共享 Protobuf 契约
sdk/rust/               Rust SDK 骨架
openspec/               项目规范和变更记录
docs/ai-context/        AI 可读取的工程上下文
```

## 第一阶段目标

第一阶段重点是项目建模，而不是直接实现功能：

1. 初始化仓库架构。
2. 明确模块职责。
3. 创建第一个 OpenSpec change。
4. 在 `openspec/prompt.md` 中记录 AI Prompt 和人工决策。
