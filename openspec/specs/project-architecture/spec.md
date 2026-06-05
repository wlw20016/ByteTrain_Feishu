# 项目架构能力

## Purpose

定义 ByteTrain Feishu Android-only 项目的稳定模块边界、OpenSpec 证据要求和 AI 可读上下文入口。
## Requirements
### Requirement: 模块边界必须清晰

仓库 MUST 区分 App 入口、功能模块、共享抽象、protobuf 契约、Rust SDK 代码、OpenSpec 文档和 AI 上下文文档。

#### Scenario: AI 读取项目结构

- Given AI 助手需要理解仓库
- When 它打开 `docs/ai-context/project-structure.md`
- Then 它可以识别每个顶层模块及其职责

### Requirement: 重要变更必须可追踪

重要实现变更 MUST 有对应的 OpenSpec change，包含 proposal、design、tasks 和相关证据。

#### Scenario: 功能任务被实现

- Given 一个 P0 功能已实现
- When 任务被标记完成
- Then `tasks.md` 中包含 PR、构建、测试或评审记录等证据

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

