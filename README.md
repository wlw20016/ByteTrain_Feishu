# ByteTrain Feishu Android

本仓库是一个由 AI、OpenSpec 和 Bazel 辅助驱动的 Android 工程实践项目，目标是实现包含消息和邮箱主链路的飞书风格移动端应用，并沉淀可追踪的 SDK、protobuf、构建和验收证据。

## 项目范围

- Kotlin Android App，包含消息和邮箱两个主 Tab。
- `features/` 按功能模块拆分 message 和 mail。
- `shared/` 提供通用分页、导航和 UI 模型。
- `proto/` 定义 Kotlin 与 Rust 共享的数据契约。
- `sdk/rust/` 实现 Rust SDK 的分页、异步读取和 protobuf 边界。
- `openspec/` 记录需求、设计、任务和归档变更。
- `docs/` 记录项目结构、AI context、构建证据和最终验收材料。

## 目录入口

```text
app/                     Android App 入口
features/message/        消息功能模块
features/mail/           邮箱功能模块
shared/                  通用模型、列表和导航
proto/                   共享 protobuf 契约
sdk/rust/                Rust SDK
scripts/commands/           构建和最终验收入口脚本
scripts/checks/          按功能分组的验证脚本
openspec/                OpenSpec 规格和变更记录
docs/                    文档索引、AI context 和验收证据
```

## 常用文档

- 文档索引：`docs/README.md`
- 项目结构：`docs/project/project-structure.md`
- 模块边界：`docs/project/module-boundaries.md`
- 构建命令：`docs/ai-context/build-system/build-commands.md`
- Rust SDK 协议草案：`docs/ai-context/sdk/rust-sdk-async-protobuf-contract.md`

## 常用命令

```powershell
cargo test --manifest-path sdk/rust/Cargo.toml
powershell -ExecutionPolicy Bypass -File .\scripts\commands\ide-build.ps1 -Target rust
```
