# 任务：complete-ai-evidence-archive

## 1. AI 证据

- [x] AI-ARCH-001 更新 `openspec/prompt.md`，补齐所有主要阶段的 prompt、关键上下文、AI 结论、人工决策和最终结果。
  - 证据：`openspec/prompt.md` 已追加 Android UI、SDK/protobuf runtime、Bazel/IDE、最终 AI 证据与归档阶段。
- [x] AI-ARCH-002 新增或更新 `docs/ai-context/` 下的最终 AI 证据文档，覆盖被采纳和被拒绝的 AI 建议。
  - 证据：`docs/ai-context/final-ai-evidence.md` 已记录 accepted/rejected/deferred AI suggestions、human decisions 和 final result。
- [x] AI-ARCH-003 确保每个已完成 P0 任务都在所属 `tasks.md` 中记录本地证据。
  - 证据：`docs/ai-context/p0-evidence-audit.md` 已按正式验收范围审计 completed P0 scope、owning tasks 和 local evidence；可选飞书任务已明确排除在正式本地证据之外。

## 2. AI 可读上下文

- [x] AI-ARCH-004 更新 `docs/ai-context/project-structure.md`，使其与最终仓库结构一致。
  - 证据：`docs/ai-context/project-structure.md` 已覆盖 app provider、SDK-backed repository、tests、scripts、VS Code helper、final evidence docs 和 archive docs。
- [x] AI-ARCH-005 更新 `docs/ai-context/module-boundaries.md`，使其与最终 Bazel query 证据一致。
  - 证据：`docs/ai-context/module-boundaries.md` 已按 2026-06-05 final query 记录 `AppRepositoryProvider.kt` 和 app/feature/shared 显式依赖边界。
- [x] AI-ARCH-006 更新 `docs/ai-context/build-commands.md`，记录最终 Bazel 验证命令和结果。
  - 证据：`docs/ai-context/build-commands.md` 已记录最终 Bazel delivery script、app/proto/shared-feature/Rust/query 结果、Gradle 非最终验收边界和 iOS/Xcode 跳过原因。
- [x] AI-ARCH-007 更新 `docs/ai-context/common-build-errors.md`，记录剩余最终验证阻塞和修复方式。
  - 证据：`docs/ai-context/common-build-errors.md` 已记录最终剩余环境阻塞和修复方式，并标注当前无未记录的最终验证阻塞。

## 3. OpenSpec 校验

- [x] AI-ARCH-008 对每个活跃 change 运行 `openspec validate <change> --strict`，并记录结果摘要。
  - 证据：`docs/ai-context/openspec-archive-audit.md` 已记录所有 active changes 的 strict validate 命令和 `valid` 结果。
- [x] AI-ARCH-009 识别被新最终化 change 取代的活跃 change，并记录取代决策。
  - 证据：`docs/ai-context/openspec-archive-audit.md` 已记录 `init-project-architecture` final Bazel 任务、`improve-ai-context` 文档任务和可选飞书任务的 superseded/deferred 决策。

## 4. 归档与复盘

- [x] AI-ARCH-010 归档满足归档条件的已完成 OpenSpec changes。
  - 证据：`docs/ai-context/openspec-archive-audit.md` 已记录 8 个 archived changes、生成/更新的 specs，以及归档时修复 `project-architecture` spec 格式的说明。
- [x] AI-ARCH-011 新增最终复盘文档，说明可复用的 AI 模板、无效建议和剩余限制。
  - 证据：`docs/ai-context/final-retrospective.md` 已记录 reusable prompts、effective patterns、accepted/rejected suggestions、build failures 和 remaining limitations。
- [x] AI-ARCH-012 更新本地 OpenSpec tasks 中的最终证据链接；飞书多维表格只作为可选入口链接，不作为正式验收证据。
  - 证据：本文件、`openspec/changes/improve-ai-context/tasks.md` 和 `openspec/changes/archive/*/tasks.md` 保留本地证据链接；`docs/ai-context/p0-evidence-audit.md` 汇总正式验收证据；飞书多维表格更新已按 `build/任务完成次序列表.md` 保留为第五阶段可选任务。
