# AI Prompt 沉淀记录

本文档记录项目中可复用的 Prompt、AI 输出摘要、人工决策和最终结果。

## 1. 需求拆解

### Prompt

请将“仿飞书 Android App，包含消息和邮箱两个 Tab”拆解为页面、数据、交互、性能、构建、测试六类需求，并给出 P0/P1 优先级。

### 关键上下文

- App 使用 Kotlin。
- 消息和邮箱两个 Tab 各需要 10000 条 mock 数据。
- 列表需要支持分页和详情页跳转。
- 后续阶段会引入 Rust SDK、protobuf、Bazel 和 AI 可读取工程上下文。

### AI 输出摘要

- 页面：消息列表、邮箱列表、通用详情页、App Tab 容器。
- 数据：mock 数据、分页游标、异步加载边界。
- 交互：Tab 切换、点击 item、加载更多、加载和错误恢复。
- 性能：稳定 item key、分页渲染、避免一次性渲染全部数据。
- 构建：先建立清晰模块边界，再逐步接入 Bazel target。
- 测试：Repository 分页、UI 状态、详情跳转、构建验证。

### 人工决策

- 采纳六类需求拆解方式。
- 先初始化架构和文档。
- 在 Gradle 或 Bazel 工具链确定前，不实现完整 Android UI。

### 最终结果

仓库架构和 OpenSpec 骨架已初始化。

## 2. 风险识别

### Prompt

请识别 Android UI、Rust SDK、protobuf、Bazel、OpenSpec 文档流程中的主要风险，并给出规避建议。

### AI 输出摘要

- 如果一次性渲染 10000 条数据，Android 列表性能可能下降。
- 如果没有 proto 契约，Kotlin 与 Rust 的边界可能变得不清晰。
- 如果早期不拆模块，Bazel target 边界可能过宽。
- 如果 tasks 不记录证据，OpenSpec 文档可能和实现脱节。

### 人工决策

- 初始化阶段就定义 feature、shared、proto、SDK 和 docs 边界。
- 增加 `docs/ai-context/`，方便 AI 读取项目结构和构建上下文。
- 在进入实现前，先给 OpenSpec tasks 加上证据记录口径。

### 最终结果

已创建模块边界和 AI 上下文文档。

## 3. 接口草案

### Prompt

请为消息和邮箱列表设计一套可复用的数据模型，并考虑后续由 Rust SDK 通过 protobuf 返回数据。

### AI 输出摘要

- 消息和邮箱保留各自独立的领域模型。
- 共享分页状态应使用泛型。
- Protobuf 应定义 item 消息，以及共享的分页请求和分页信息。

### 人工决策

- 为消息和邮箱创建 Kotlin 领域模型占位。
- 创建共享的 `PagingUiState`。
- 创建消息、邮箱和分页的初始 proto 文件。

### 最终结果

领域模型和 proto 骨架已初始化。

## 4. Bazel 目标规划

### Prompt

请规划该项目未来的 Bazel target，包括 Android App、UI 模块、Rust SDK、proto 和测试模块。

### AI 输出摘要

规划中的 targets：

- `//app:app`
- `//features/message:message_ui`
- `//features/message:message_domain`
- `//features/message:message_data`
- `//features/mail:mail_ui`
- `//features/mail:mail_domain`
- `//features/mail:mail_data`
- `//shared/list:paging`
- `//shared/navigation:navigation`
- `//proto:message_proto`
- `//proto:mail_proto`
- `//sdk/rust:feed_sdk`

### 人工决策

- 当前先增加 BUILD 占位文件。
- 具体 Bazel rule 接入留到后续 OpenSpec change 中处理。

### 最终结果

已在规划模块中创建 Bazel 占位文件。

## 5. 测试用例生成

### Prompt

请为消息 Tab 和邮箱 Tab 生成 P0 测试用例，覆盖列表展示、分页、点击跳转、空态、加载态和错误态。

### AI 输出摘要

- 初始加载展示列表内容。
- 空数据展示空态。
- Repository 失败展示错误态。
- 加载更多会追加数据。
- 点击详情时携带 item id 跳转。

### 人工决策

- 先在 OpenSpec 中记录测试作为计划验收项。
- 等 Android 和 Kotlin 测试工具链确定后，再添加可执行测试。

### 最终结果

测试预期已记录到第一个 OpenSpec change 中。
