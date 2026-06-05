# 项目结构

本文档为 AI 工具提供稳定的仓库结构说明。当前仓库是 Android-only 交付，最终构建验收以 Bazel 为准。

## 顶层目录

- `app/`：Android 应用入口、组合根、manifest、资源和 app 级测试。
- `features/message/`：消息 Tab 的领域模型、repository、SDK adapter、UI mapper 和 Compose UI。
- `features/mail/`：邮箱 Tab 的领域模型、repository、SDK adapter、UI mapper 和 Compose UI。
- `shared/`：跨 feature 复用的分页模型、导航常量和统一 UI 展示模型。
- `proto/`：跨语言数据契约，包含 paging、message、mail proto 和 Bazel proto targets。
- `sdk/rust/`：Rust SDK 数据生成、分页逻辑、单测和 Bazel Rust targets。
- `scripts/`：聚焦检查脚本、IDE 构建入口、最终 Bazel 验证入口和 git hook 辅助脚本。
- `tools/vscode-bazel-helper/`：最小 VS Code/Trae Bazel helper 插件原型。
- `.vscode/`：VS Code/Trae tasks，调用 `scripts/ide-build.ps1`。
- `docs/ai-context/`：AI 可读项目上下文、构建证据、错误记录、最终 AI evidence 和复盘文档。
- `openspec/`：项目规范、变更、任务、prompt 沉淀和归档记录。
- `build/`：本地分析产物和 `任务完成次序列表.md`，不作为源码模块边界。

## App runtime

- `app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt`：Android UI 入口和主导航。
- `app/src/main/kotlin/com/bytetrain/feishuclone/AppRepositoryProvider.kt`：app 级 repository 组合根，默认构造 SDK-backed repository，并保留 mock fallback。
- `app/src/main/res/drawable/`：底部导航图标资源。
- `app/src/test/kotlin/.../SdkMessageRepositoryTest.kt` 和 `SdkMailRepositoryTest.kt`：SDK-backed repository focused tests。

## Feature boundaries

- `features/message/domain/` 和 `features/mail/domain/`：领域模型与 repository interface。
- `features/*/data/`：mock repository、SDK repository adapter 和 runtime SDK client。
- `features/*/mapper/`：领域模型到 shared UI model 的映射。
- `features/*/ui/`：列表、详情和滚动加载更多 UI。

Feature 可以依赖 `shared/ui`，不能反向依赖 `app`。Shared 不能依赖具体 feature。

## Bazel targets

主要 target 记录在 `docs/ai-context/module-boundaries.md` 和 `docs/ai-context/build-commands.md`：

- `//app:app`
- `//app:app_lib`
- `//shared/list:list`
- `//shared/navigation:navigation`
- `//shared/ui:ui_models`
- `//features/message:domain|data|mapper|ui|message`
- `//features/mail:domain|data|mapper|ui|mail`
- `//proto:paging_proto`
- `//proto:message_proto`
- `//proto:mail_proto`
- `//proto:feed_proto`
- `//sdk/rust:bytetrain_feed_sdk`
- `//sdk/rust:bytetrain_feed_sdk_test`

## Evidence documents

- `docs/ai-context/final-bazel-delivery-evidence.md`：最终 Bazel app/proto/feature/Rust/query evidence。
- `docs/ai-context/final-ai-evidence.md`：最终 AI prompt、建议、人工决策和结果汇总。
- `docs/ai-context/openspec-archive-audit.md`：OpenSpec validate、superseded 和 archive 决策。
- `docs/ai-context/final-retrospective.md`：可复用 prompt、有效/无效建议和剩余限制。
- `docs/ai-context/common-build-errors.md`：真实构建失败、环境阻塞、根因、修复和重试命令。

## 编辑约定

- 功能专属代码应放在对应 feature 目录内。
- 可复用的分页、导航和展示模型应放在 `shared/`。
- `proto/` 是 Kotlin 与 Rust 之间的数据契约边界；runtime 使用 protobuf-shaped mapping 时必须记录在 OpenSpec 或 AI context 文档。
- 重要设计决策应先记录到 OpenSpec，再进入实现。
- 完成 OpenSpec task 前必须写入本地证据链接或命令摘要。
- 飞书多维表格只作为可选入口链接，不作为正式验收证据。
