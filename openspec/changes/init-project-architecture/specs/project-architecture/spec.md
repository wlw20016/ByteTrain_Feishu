# 项目架构能力增量

## ADDED Requirements

### Requirement: 仓库架构 MUST 遵循推荐模块布局

仓库 MUST 区分 App 入口、功能模块、共享抽象、protobuf 契约、Rust SDK 代码、OpenSpec 文档和 AI 可读上下文文档。

#### Scenario: AI 读取项目结构

- Given AI 助手需要理解仓库
- When 它打开 `docs/ai-context/project-structure.md`
- Then 它可以识别每个顶层模块及其职责

### Requirement: Prompt 证据 MUST 存储在 OpenSpec 中

重要 AI prompt、AI 结论、人工决策和最终结果 MUST 记录在 OpenSpec 文档或相关仓库证据中。

#### Scenario: 功能任务被实现

- Given 一个 P0 功能任务已经实现
- When 任务被标记完成
- Then 相关 `tasks.md` 条目包含 PR、构建、测试、人工验收或决策证据
