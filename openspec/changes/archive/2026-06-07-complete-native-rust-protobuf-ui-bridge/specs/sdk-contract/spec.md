## MODIFIED Requirements

### Requirement: SDK 与 UI MUST 共享显式数据契约

消息、邮箱和分页契约 MUST 使用 protobuf 表达，并与 Kotlin 领域模型保持一致。SDK-backed 主路径 MUST 使用 protobuf bytes 作为 Android SDK client 与 Rust SDK bridge 之间的通信载体；仅做到字段语义同构但没有 protobuf bytes 传输，不满足本要求。

#### Scenario: 请求消息分页数据

- **WHEN** UI 使用页大小和 cursor 通过 SDK-backed repository 请求消息分页数据
- **THEN** Android SDK client 将请求编码为 `PageRequest` protobuf bytes，经 Rust SDK bridge 获取 `MessagePageResponse` protobuf bytes，并向 repository 返回包含消息 item、next cursor 和 has-more 信息的结果

#### Scenario: 请求邮箱分页数据

- **WHEN** UI 使用页大小和 cursor 通过 SDK-backed repository 请求邮箱分页数据
- **THEN** Android SDK client 将请求编码为 `PageRequest` protobuf bytes，经 Rust SDK bridge 获取 `MailPageResponse` protobuf bytes，并向 repository 返回包含邮箱 item、next cursor 和 has-more 信息的结果
