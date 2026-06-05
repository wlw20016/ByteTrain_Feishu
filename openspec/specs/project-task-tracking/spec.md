# project-task-tracking Specification

## Purpose
TBD - created by archiving change plan-project-task-tracking. Update Purpose after archive.
## Requirements
### Requirement: 项目任务 MUST 映射到仓库证据

每个 P0 或 P1 项目任务 MUST 能够从任务表追踪到 OpenSpec change 和仓库产出。

#### Scenario: 已跟踪任务被标记完成

- Given 一个任务在飞书多维表格中被标记为完成
- When 评审者打开该任务关联的 OpenSpec change
- Then 对应 `tasks.md` 条目包含实现、构建、测试或人工验收证据

### Requirement: 飞书多维表格 MUST 作为日常入口而非正式证据源

飞书多维表格 MUST 用于负责人和进度跟踪，OpenSpec 才是正式验收证据记录。

#### Scenario: 飞书状态与 OpenSpec 证据不一致

- Given 飞书多维表格中某任务状态为完成
- When 关联的 OpenSpec task 没有验收证据
- Then 该任务不能视为通过项目验收

