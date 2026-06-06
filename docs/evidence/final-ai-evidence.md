# Final AI Evidence

记录日期：2026-06-05。

本文档汇总项目主要阶段的 AI 参与、采纳建议、拒绝建议、人工决策和最终证据。正式验收以本仓库文件为准；飞书多维表格只作为可选入口链接。

## Evidence map

| 阶段 | Prompt / context | AI 结论 | 人工决策 | 最终证据 |
| --- | --- | --- | --- | --- |
| 需求拆解与架构初始化 | `openspec/prompt.md` 1-4 节；`init-project-architecture` | 按 app/features/shared/proto/sdk/docs 拆分；先保留 Bazel 占位 | 采纳分层与 OpenSpec-first 流程 | `openspec/changes/init-project-architecture/tasks.md`、`docs/project/project-structure.md` |
| 任务跟踪 | `plan-project-task-tracking` | OpenSpec tasks 是正式验收证据，飞书表格只做进度入口 | 采纳 evidence-first；外部表格不做唯一证据 | `openspec/changes/plan-project-task-tracking/tasks.md`、`build/任务完成次序列表.md` |
| Android UI 主链路 | `add-ui-main-flow`、`polish-ui-navigation-names` | 共享 UI 模型、feature 独立领域模型、分页状态矩阵、移动端详情页 | 采纳统一 UI 模型；拒绝暴露调试字段 | `docs/ai-context/ui/ui-main-flow-release-evidence.md`、`docs/ai-context/ui/ui-main-flow-doc-001.md`、相关 check 脚本 |
| SDK/protobuf 运行时 | `add-sdk-contract`、`connect-sdk-protobuf-runtime` | Provider + SDK client interface + mock fallback；protobuf-shaped mapping | 采纳 SDK-backed repository；暂不接 native Rust FFI 和 generated Kotlin protobuf runtime | `docs/ai-context/sdk/sdk-adapter-evidence.md`、`openspec/changes/connect-sdk-protobuf-runtime/tasks.md` |
| Bazel 构建迁移 | `wire-bazel-build` | 用 Bzlmod pin Android/Kotlin/proto/Rust rules；通过 query 固化边界 | 采纳 Bazel final acceptance；Gradle 只保留为历史/开发辅助 | `docs/ai-context/build-system/build-commands.md`、`docs/project/module-boundaries.md` |
| IDE/Trae 协作 | `improve-ai-context`、`prototype-ide-build-plugin` | 统一 `scripts/commands/ide-build.ps1`，VS Code tasks 和插件原型复用同一入口 | 采纳最小插件原型；不复制 Bazel 命令到多个入口 | `docs/ai-context/build-system/ide-bazel-workflow.md`、`tools/vscode-bazel-helper/` |
| 最终 Bazel 交付 | `verify-final-bazel-delivery` | 单一最终脚本运行 app/proto/features/Rust/query 并生成 evidence | 采纳脚本化最终验证；拒绝 Gradle/iOS 作为 Android-only 最终验收项 | `scripts/commands/verify-final-bazel-delivery.ps1`、`docs/evidence/final-bazel-delivery-evidence.md` |
| AI 证据与归档 | `complete-ai-evidence-archive` | 本地化 AI evidence、validate/archive 审计和复盘 | 采纳本地文件作为正式验收；飞书表格留作可选第五阶段 | 本文档、`docs/evidence/openspec-archive-audit.md`、`docs/evidence/final-retrospective.md` |

## Accepted AI suggestions

- 使用 OpenSpec change + `tasks.md` 作为实现和验收主线。
- 将 App、feature、shared、proto、Rust SDK、docs 拆成明确目录边界。
- 用 `UnifiedListItem`、`DetailModel`、`PagingUiState` 减少 UI 重复，同时保留 message/mail 独立领域模型。
- 用 focused PowerShell 检查脚本记录 UI、mapper、paging、IDE 插件和最终交付证据。
- 通过 `AppRepositoryProvider` 把 SDK-backed repository 接入 app 组合根，并保留 mock fallback。
- 以 Bazel build/test/query 作为最终验收依据，Gradle 仅作为早期 Android UI 开发和历史证据。
- 记录真实构建失败和环境阻塞，包括外部依赖下载超时、Android/JDK 配置、非 ASCII 主机名日志噪声、`Access is denied`。
- 用 `docs/ai-context/` 为未来 AI 助手提供稳定项目上下文。

## Rejected or deferred AI suggestions

- 拒绝一次性渲染 10000 条列表数据；最终采用分页加载和滚动触发加载更多。
- 拒绝让 shared 依赖具体 feature，或让 feature 反向依赖 app。
- 拒绝只使用 Gradle 证明最终完成；最终验收必须包含 Bazel app/proto/features/Rust/query 证据。
- 暂缓 native Rust FFI runtime；当前阶段使用 Kotlin runtime bridge 保持 Rust SDK 分页语义和 fallback 行为。
- 暂缓 generated Kotlin protobuf runtime 接入 UI 主链路；当前阶段通过 proto Bazel target 和 protobuf-shaped mapping 证明契约。
- 拒绝把 `bazel build //...` 大日志直接贴入文档；只记录命令、结果摘要、根因和重试命令。
- 拒绝把飞书多维表格作为唯一验收记录；正式证据必须在仓库内。

## Human decisions

- 优先完成 Android-only 交付；iOS、UIKit、AutoLayout、Xcode 相关命令不纳入最终验收。
- 对外部工具或本机环境失败保持可追踪记录，不把环境问题伪装成源码通过或失败。
- 已完成任务必须在本地 `tasks.md` 或 `docs/ai-context/` 中保留证据链接。
- 归档只处理任务完整、strict validate 通过且长期 spec 已可承接的 changes。

## Final result

截至 2026-06-05，仓库具备：

- Android app 主链路和移动端 UI 证据。
- SDK-backed repository runtime path、mock fallback 和 protobuf-shaped mapping 证据。
- Bazel app/proto/shared-feature/Rust test/query 最终验证证据。
- IDE/Trae 构建辅助入口和插件原型。
- AI prompt、AI 结论、人工决策、采纳/拒绝建议、构建错误和归档审计文档。
