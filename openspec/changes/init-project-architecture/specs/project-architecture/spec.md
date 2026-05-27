# 项目架构变更

## ADDED Requirements

### Requirement: 仓库架构遵循推荐模块布局

仓库必须包含 app、feature、shared、proto、Rust SDK、OpenSpec 和 AI context 区域，并与 `任务说明解读.md` 保持一致。

#### Scenario: 项目完成初始化

- Given 开发者打开仓库根目录
- When 查看顶层目录
- Then 可以看到 `app/`、`features/`、`shared/`、`proto/`、`sdk/`、`openspec/` 和 `docs/ai-context/`

### Requirement: Prompt 证据存储在 OpenSpec 中

项目必须在 OpenSpec 中记录可复用的 AI Prompt、AI 输出摘要、人工决策和最终结果。

#### Scenario: 需要审查 AI 参与证据

- Given 评审者检查 AI 参与证据
- When 打开 `openspec/prompt.md`
- Then 可以看到需求拆解、风险识别、接口草案、Bazel 目标规划和测试生成相关 Prompt
