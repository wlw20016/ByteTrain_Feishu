# SDK 契约能力增量

## ADDED Requirements

### Requirement: SDK 与 UI MUST 共享显式数据契约

消息、邮箱和分页契约 MUST 使用 protobuf 表达，并与 Kotlin 领域模型保持一致。

#### Scenario: 请求消息分页数据

- Given UI 使用页大小和 cursor 请求消息分页数据
- When SDK 契约被使用
- Then 响应包含消息 item、next cursor 和 has-more 信息，并符合约定语义

### Requirement: Rust SDK MUST 提供分页 mock 数据

Rust SDK MUST 为开发和验证提供确定性的消息和邮箱分页 mock API。

#### Scenario: 请求最后一页

- Given cursor 指向最后一页
- When SDK 返回数据
- Then `has_more` 为 false，并且不会暴露无效的下一页

### Requirement: UI MUST 依赖 repository 接口而不是 SDK 内部实现

Android UI 代码 MUST 使用 repository 接口和 adapter，使数据源能够从 Kotlin mock 切换为 SDK-backed 实现。

#### Scenario: 数据源切换

- Given App 从 mock repository 切换为 SDK adapter
- When 列表页面加载数据
- Then UI 渲染和分页行为保持不变
