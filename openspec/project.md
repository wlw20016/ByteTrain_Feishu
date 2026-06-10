# ByteTrain 仿飞书 Android

## 目标

构建一个包含消息和邮箱两个 Tab 的仿飞书 Android App，同时实践 AI + Bazel + OpenSpec 驱动的工程交付流程。

## 当前能力基线

- 仓库架构已初始化。
- 模块边界已文档化。
- AI Prompt 证据记录在 `openspec/prompt.md`。

## 交付原则

- 重要变更先创建 OpenSpec change，再进入实现。
- AI 可以生成需求、风险、设计、接口、Bazel target 和测试初稿，但人工决策必须记录下来。
- 有构建和测试结果时，应在 `tasks.md` 中关联证据。
