# Design: plan-project-task-tracking

## 跟踪模型

飞书多维表格用于日常任务推进，OpenSpec 用于仓库内可 review、可归档的正式证据。

建议飞书多维表格字段：

- 任务 ID
- 阶段
- 模块
- 任务名称
- 任务说明
- 优先级
- 负责人
- 状态
- 预计人天
- 依赖任务
- 验收标准
- OpenSpec Change
- 代码或文档产出
- 构建或验证命令

## Change 映射

- `plan-project-task-tracking`：任务台账和跟踪流程。
- `add-ui-main-flow`：Android UI 主链路、mock 数据、列表详情页、状态矩阵。
- `add-sdk-contract`：proto、Rust SDK mock、异步数据边界。
- `wire-bazel-build`：Bazel rules、target、query、构建证据。
- `improve-ai-context`：AI prompt 证据、排障文档、IDE 工作流。

## 工作流程

1. 每个任务先在飞书多维表格中创建或更新记录。
2. 每个 P0/P1 任务必须关联一个 OpenSpec change ID。
3. 实现前更新对应 change 的 `tasks.md`。
4. 实现后在 `tasks.md` 中记录 PR、构建、测试或人工验收证据。
5. 验收后归档 OpenSpec change。

## 人工决策

任务表面向日常协作，OpenSpec 面向正式验收和长期证据。任务只有在对应 OpenSpec task 中补齐证据后，才视为通过验收；不能只依赖飞书多维表格里的状态字段。
