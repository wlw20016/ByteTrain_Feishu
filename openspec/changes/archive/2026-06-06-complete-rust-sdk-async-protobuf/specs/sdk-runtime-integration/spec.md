## ADDED Requirements

### Requirement: Rust SDK must expose async page read APIs

Rust SDK MUST 暴露用于读取消息和邮箱分页的 async API，并保留当前分页语义。

#### Scenario: Async message page read

- **WHEN** 调用方通过 Rust async API 请求消息第一页
- **THEN** SDK 返回包含 items、`next_cursor` 和 `has_more` 的分页结果，语义与现有 SDK 分页行为一致

#### Scenario: Async mail page read

- **WHEN** 调用方使用非空 cursor 请求邮箱分页
- **THEN** SDK 将 cursor 作为下一页位置，并返回匹配的邮箱分页元数据

### Requirement: Rust SDK must support protobuf request and response conversion

Rust SDK MUST 为分页请求和 message/mail 分页响应提供经过测试的 protobuf 边界。

#### Scenario: Message response round trip

- **WHEN** message page response 被编码为 protobuf bytes 后再解码
- **THEN** 解码后的 response 保留 message page contract 所需的 items 和分页元数据

#### Scenario: Mail response round trip

- **WHEN** mail page response 被编码为 protobuf bytes 后再解码
- **THEN** 解码后的 response 保留 mail page contract 所需的 items 和分页元数据

### Requirement: Rust SDK protobuf failures must be structured

Rust SDK MUST 针对非法请求和 protobuf 转换失败返回结构化错误。

#### Scenario: Invalid protobuf input

- **WHEN** SDK 收到非法 page request protobuf bytes
- **THEN** 返回可在测试中匹配的 decode error，且不需要解析 display text
