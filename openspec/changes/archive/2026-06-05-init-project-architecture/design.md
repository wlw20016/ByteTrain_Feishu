# Design: init-project-architecture

## 架构

仓库采用 `任务说明解读.md` 中推荐的整体技术架构：

```text
app/
features/message/
features/mail/
shared/
proto/
sdk/rust/
openspec/
docs/ai-context/
```

## 模块职责

- `app/`：应用启动和应用级导航。
- `features/message/`：消息 Tab 的 domain、data 和 UI。
- `features/mail/`：邮箱 Tab 的 domain、data 和 UI。
- `shared/`：通用分页、导航和 UI 抽象。
- `proto/`：跨语言数据契约。
- `sdk/rust/`：Rust SDK 数据和异步服务边界。
- `docs/ai-context/`：AI 可读取的工程上下文。
- `openspec/`：需求、设计、任务、Prompt 证据和归档记录。

## Bazel 策略

本 change 只创建 `BUILD.bazel` 占位文件。具体 Bazel rule 会在 Android、Kotlin、Rust 和 proto 工具链方案确定后再接入。

## 人工决策

- 模块命名面向 Jetpack Compose 使用场景，但当前不锁死 UI 实现方式。
- 消息和邮箱保留独立领域模型。
- 分页状态放入 `shared/list`，因为两个 Tab 都需要复用。
- 按要求将 Prompt 记录在 `openspec/prompt.md`。
