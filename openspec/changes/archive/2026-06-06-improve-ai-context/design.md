# Design: improve-ai-context

## 文档入口

持续维护以下 AI 可读文档：

- `docs/project/project-structure.md`
- `docs/project/module-boundaries.md`
- `docs/ai-context/build-system/build-commands.md`
- `docs/ai-context/build-system/common-build-errors.md`
- `docs/ai-context/build-system/ide-bazel-workflow.md`
- `openspec/prompt.md`

## 证据模型

每次重要 AI 协作应记录：

- Prompt
- 提供给 AI 的上下文
- AI 结论
- 人工决策
- 采纳和拒绝的建议
- 最终结果
- 构建、测试或人工验收证据

## IDE 工作流

IDE 工作流文档需要说明团队如何在 Trae 或 VS Code 中编辑、索引、构建、测试和诊断 Kotlin、Rust、proto、BUILD 文件。

## 构建辅助

插件或构建辅助任务为 P2。如果时间有限，先交付设计文档和最小命令包装脚本即可，不强行实现完整插件。
