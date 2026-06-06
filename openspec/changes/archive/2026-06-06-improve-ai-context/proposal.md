# Proposal: improve-ai-context

## 背景

课题考察的不只是 App 功能结果，还包括 AI 参与工程交付的证据：prompt 记录、人工决策、构建排障、IDE 工作流和可复用项目上下文。

## 变更内容

- 维护需求分析、方案设计、实现、测试和构建排障阶段的 AI prompt 记录。
- 让 AI 可读文档随代码演进保持同步。
- 为 Trae 或 VS Code 补充 IDE + Bazel 协作说明。
- 记录构建失败、修复方式、query 输出和阶段复盘。
- 时间允许时，补充最小构建辅助脚本或插件方案。

## 影响范围

- 涉及 `docs/ai-context`、`openspec/prompt.md` 和各 change 的任务证据。
- 不直接改变 App 行为。
- 支撑最终验收和后续 AI 协作。
