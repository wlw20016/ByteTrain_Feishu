# ai-context Specification

## Purpose
TBD - created by archiving change improve-ai-context. Update Purpose after archive.

## Requirements
### Requirement: AI 协作证据 MUST 被记录

重要 AI 辅助工作 MUST 记录 prompt 上下文、AI 结论、人工决策和最终结果。

#### Scenario: 使用 AI 辅助实现功能

- Given AI 参与生成设计、代码、测试或排障建议
- When 功能任务被标记为完成
- Then 相关 OpenSpec 证据包含 AI 的建议，以及人工采纳或拒绝的内容

### Requirement: AI 可读工程文档 MUST 保持更新

项目 MUST 维护结构、模块边界、构建命令、常见错误和 IDE 工作流等 AI 可读文档。

#### Scenario: 后续 AI 助手诊断构建问题

- Given AI 助手读取 `docs/ai-context`
- When 它分析构建失败
- Then 它可以使用当前模块边界、命令和已知错误记录进行推理

### Requirement: IDE 与 Bazel 协作方式 MUST 文档化

项目 MUST 文档化 Trae 或 VS Code 如何配合 Bazel 命令和项目索引。

#### Scenario: 组员配置项目环境

- Given 组员按照 IDE/Bazel 工作流文档操作
- When 组员运行文档中的命令
- Then 组员可以编辑、构建和诊断项目，而不依赖聊天记录
