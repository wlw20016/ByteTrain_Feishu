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

## 6. Android UI 主链路实现

### Prompt

请实现仿飞书 Android App 的消息和邮箱双 Tab 主链路，包含真实 `MainActivity`、列表、详情、分页状态、10000 条 mock 数据和可执行验证入口。

### 关键上下文

- 项目先用 Gradle 作为 Android UI 快速验证入口，最终验收迁移到 Bazel。
- UI 需要保持移动端体验，不能展示内部调试字段。
- 消息和邮箱需要共享统一列表/详情 UI 模型，但领域模型保持独立。

### AI 输出摘要

- 建议将 App 入口、feature、shared UI/list/navigation 分层。
- 建议使用 `UnifiedListItem`/`DetailModel` 承载列表和详情展示。
- 建议通过 repository 分页和 mapper 测试证明 10000 条数据不是一次性渲染。

### 人工决策

- 采纳共享 UI 模型和 feature 独立领域模型。
- 采纳 Gradle 作为 UI 初期构建入口，但明确它不是最终验收依据。
- 通过聚焦 PowerShell 检查脚本沉淀人工验收证据。

### 最终结果

`add-ui-main-flow` 与 `polish-ui-navigation-names` 完成消息/邮箱主链路、移动端详情页、图标 tab、滚动加载更多和相关证据文档。

## 7. SDK/protobuf 运行时闭环

### Prompt

请将 Android UI 从直接使用 Kotlin mock repository 调整为通过 app 级 provider 构造 SDK-backed repository，同时保留 mock fallback，并确认 proto、Rust SDK 与 Kotlin 字段映射一致。

### 关键上下文

- Rust SDK 已提供确定性分页语义。
- 当前阶段不要求接入真实 native Rust FFI 或生成 Kotlin protobuf runtime。
- UI 层不能依赖 SDK DTO、protobuf DTO 或桥接实现细节。

### AI 输出摘要

- 建议新增 `AppRepositoryProvider` 作为组合根。
- 建议将 runtime bridge 隔离在 `MessageSdkClient`/`MailSdkClient` 后面。
- 建议 invalid cursor 和 SDK 失败时委托 mock fallback。
- 建议把 protobuf-shaped request/response 映射写入设计文档。

### 人工决策

- 采纳 provider + SDK client interface + fallback 方案。
- 暂不采纳“直接把生成 Kotlin protobuf 绑定接入 UI 主链路”，因为当前 Bazel/proto 目标只验证契约，不负责 runtime 生成代码。
- 暂不采纳 native Rust FFI 接入，保留为后续扩展。

### 最终结果

`connect-sdk-protobuf-runtime` 完成 app provider、runtime SDK clients、SDK-backed repositories、focused unit tests、Rust tests、Bazel proto/app/SDK 验证和 `docs/ai-context/sdk-adapter-evidence.md`。

## 8. Bazel 与 IDE 工程化闭环

### Prompt

请为 Android app、shared/feature Kotlin、proto、Rust SDK 和 app dependency query 建立 Bazel 验证路径，并提供 IDE/Trae 可调用的最小构建辅助入口。

### 关键上下文

- 最终课程验收以 Bazel build/test/query 为准。
- Gradle 可以作为开发辅助，但不能作为最终完成证据。
- Trae/VS Code 需要能调用同一组构建命令，避免命令散落。

### AI 输出摘要

- 建议 pin `rules_android`、`rules_kotlin`、`rules_proto`、`rules_rust`。
- 建议按 proto、shared、feature、app、Rust SDK、query 顺序接入 Bazel。
- 建议新增 `scripts/ide-build.ps1` 和 VS Code extension prototype，所有 IDE 命令复用脚本。

### 人工决策

- 采纳 Bzlmod 和显式 target 边界。
- 采纳 IDE 脚本/插件原型，但保留 Android Studio/Gradle 作为 UI 开发便利入口。
- 拒绝把 Gradle 构建作为最终验收依据。

### 最终结果

`wire-bazel-build`、`prototype-ide-build-plugin` 和 `verify-final-bazel-delivery` 完成最终 Bazel 构建、测试、query、IDE 辅助入口和 evidence 文件。

## 9. 最终 AI 证据与归档

### Prompt

请补齐最终 AI 证据、上下文和归档材料，使评审者无需读取聊天记录也能从仓库追踪 prompt、AI 建议、人工决策、被采纳/拒绝的方案、验证证据和归档状态。

### 关键上下文

- `build/任务完成次序列表.md` 将第四阶段定义为“补齐 AI 证据、上下文和归档”。
- 所有正式验收证据必须在本地仓库文件中，飞书多维表格只作为可选入口链接。
- 活跃 change 需要 strict validate，满足条件的 completed change 才能 archive。

### AI 输出摘要

- 建议新增最终 AI 证据文档、OpenSpec validate/archive 审计文档和复盘文档。
- 建议把被最终化 change 取代的历史任务标记为 superseded，而不是伪造外部飞书表格证据。
- 建议保留环境阻塞记录，避免把 `Access is denied`、Bazel 下载超时或本机内存限制误判为源码失败。

### 人工决策

- 采纳本地 evidence-first 方案。
- 飞书多维表格更新留作第五阶段可选项，不作为正式验收阻塞。
- 只归档任务完整、strict validate 通过、且证据已本地化的 change。

### 最终结果

`complete-ai-evidence-archive` 补齐最终 AI 证据、AI 可读上下文、strict validate 摘要、archive/superseded 决策和最终复盘文档。
