# Tasks: plan-project-task-tracking

## 1. 飞书多维表格配置

- [x] 导入本机生成的任务 TSV 文件到飞书多维表格。
- [x] 将 `状态`、`优先级`、`负责人` 配置为结构化字段。
- [x] 创建 P0 任务、按负责人、阻塞任务、待验收任务等视图。

## 2. OpenSpec 映射

- [x] 为生成的任务列表创建阶段级 OpenSpec changes。
  - 证据：`plan-project-task-tracking`、`add-ui-main-flow`、`add-sdk-contract`、`wire-bazel-build`、`improve-ai-context`。
- [x] 将 OpenSpec change ID 补充到每一条飞书多维表格任务记录中。
- [x] 在相关 `tasks.md` 中保留飞书任务 ID，便于双向追踪。

## 3. Review 流程

- [x] 明确飞书多维表格只作为日常进度跟踪入口。
- [x] 明确 OpenSpec `tasks.md` 中的证据才是正式验收依据。
- [x] 更新 PR 描述约定，至少包含 OpenSpec change ID、测试结果和 AI 使用记录。

## 4. 验收标准

- [x] 每个 P0 任务都关联到 OpenSpec change。
- [x] 每个已完成 P0 任务都在对应 `tasks.md` 中记录证据。
- [x] 最终验收时可以从飞书任务行追踪到 OpenSpec change、代码或文档 diff、构建测试或人工验收证据。
