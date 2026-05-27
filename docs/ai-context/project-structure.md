# 项目结构

本文档为 AI 工具提供稳定的仓库结构说明。

## 模块说明

- `app/`：Android 应用入口和应用级装配。
- `features/message/`：消息 Tab 功能。
- `features/mail/`：邮箱 Tab 功能。
- `shared/`：通用列表、UI 和导航抽象。
- `proto/`：跨语言数据契约。
- `sdk/rust/`：Rust SDK 骨架。
- `openspec/`：项目规范、变更、任务和 AI 协作记录。

## 编辑约定

- 功能专属代码应放在对应 feature 目录内。
- 可复用的分页和导航抽象应放在 `shared/`。
- `proto/` 是 Kotlin 与 Rust 之间的数据契约边界。
- 重要设计决策应先记录到 OpenSpec，再进入实现。
