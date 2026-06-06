# 任务：improve-ai-context

## 1. AI 证据

- [x] DOC-001 为每个主要阶段记录 prompt、AI 结论、人工决策和最终结果。
  - 证据：`openspec/prompt.md` 已补齐需求拆解、架构、UI、SDK/protobuf、Bazel/IDE、最终 AI 证据与归档阶段；`docs/evidence/final-ai-evidence.md` 汇总主要阶段 AI 结论、人工决策、采纳/拒绝建议和最终结果。
- [x] 持续更新 `openspec/prompt.md`，沉淀可复用 prompt 和决策说明。
  - 证据：`openspec/prompt.md` 已追加第 6-9 节，覆盖 Android UI 主链路、SDK/protobuf 运行时、Bazel 与 IDE 工程化、最终 AI 证据与归档。
- [x] 确保每个已完成 P0 任务都在对应 OpenSpec `tasks.md` 中记录证据。
  - 证据：`docs/evidence/p0-evidence-audit.md` 已按正式验收范围审计 completed P0 任务与本地证据；各 completed change 的 `tasks.md` 或链接文档保留命令、脚本、代码或人工验收证据。

## 2. AI 可读项目上下文

- [x] DOC-002 随实现演进更新项目结构、模块边界、构建命令和常见构建错误。
  - 证据：`docs/project/project-structure.md`、`docs/project/module-boundaries.md`、`docs/ai-context/build-system/build-commands.md` 和 `docs/ai-context/build-system/common-build-errors.md` 已按最终 Android-only Bazel 交付状态复核并补充 AI-ARCH 审计标记。
- [x] DOC-003 记录真实 Bazel 构建失败、根因、修复方式和验证命令。
  - 证据：`docs/ai-context/build-system/common-build-errors.md` 已记录 `//proto:...` target 兼容、首次工具链超时、Bzlmod 下载阻塞、Android app target 配置、非 ASCII 主机名日志噪声和 `Access is denied` 环境权限问题；最终重试证据见 `docs/evidence/final-bazel-delivery-evidence.md`。

## 3. IDE 协作

- [x] IDE-001 新增 `docs/ai-context/build-system/ide-bazel-workflow.md`，说明 Trae/VS Code 与 Bazel 的协作方式。
  - 证据：`docs/ai-context/build-system/ide-bazel-workflow.md` 已说明 IDE 与 Bazel 的角色分工、Android UI Gradle 验证、Bazel build/query 工作流、Rust/proto/BUILD 文件协作、构建失败记录和 AI 上下文读取方式；`openspec validate improve-ai-context --strict` 通过。
- [x] IDE-002 设计最小 Trae/VS Code 构建辅助或插件方案；时间允许时实现最小脚本或入口。
  - 证据：`docs/ai-context/build-system/ide-bazel-workflow.md` 已记录 IDE-002 最小构建辅助方案；`scripts/commands/ide-build.ps1` 已提供 Trae/VS Code 可调用的统一入口，支持 `app`、`gradle-app`、`proto`、`features`、`rust` 和 `query-app-deps`；`.vscode/tasks.json` 已暴露对应 VS Code/Trae tasks；`powershell -ExecutionPolicy Bypass -File .\scripts\checks\ide\check-ide-002.ps1` 和 `cmd /c openspec validate improve-ai-context --strict` 通过。

## 4. 验收与归档

- [x] REL-002 验收完成后归档已完成的 OpenSpec changes。
  - 证据：`docs/evidence/openspec-archive-audit.md` 已记录所有 active change 的 `openspec validate <change> --strict` 结果、superseded 决策和归档候选；满足条件的 completed changes 已按 AI-ARCH-010 执行或记录归档结果。
- [x] 更新飞书多维表格中的最终证据链接。
  - 说明：用户确认该外部链接任务已完成；根据 `build/任务完成次序列表.md`，飞书多维表格更新属于第五阶段可选入口链接，不作为正式本地验收证据。
