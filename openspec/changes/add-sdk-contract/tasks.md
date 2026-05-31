# Tasks: add-sdk-contract

## 1. Proto 契约

- [ ] SDK-001 扩展 `message.proto`、`mail.proto` 和 `paging.proto`，与已确认的 Kotlin 领域字段保持一致。
- [ ] SDK-002 定义消息和邮箱分页响应 message，或采用等价的共享分页响应策略。
- [ ] 在 `design.md` 或 proto 注释中记录 cursor 语义和兼容性约定。

## 2. Rust SDK

- [ ] SDK-003 实现 Rust mock 数据模型，并为消息和邮箱生成确定性的 10000 条记录。
- [ ] SDK-004 实现 Rust 消息和邮箱分页 API。
- [ ] 增加 Rust 单测，覆盖第一页、中间页、最后一页、非法 cursor 和 page size 边界。

## 3. 异步与 Adapter 边界

- [ ] SDK-005 定义 Kotlin 到 SDK 的异步边界和错误映射策略。
- [ ] SDK-006 在 UI mock 主链路稳定后实现 SDK-backed repository adapter。
- [ ] 在 SDK 集成验证完成前保留 Kotlin mock repository 作为回退路径。

## 4. 证据

- [ ] 记录 AI 生成的协议建议和人工取舍。
- [ ] 可运行时记录 `cargo test` 或 Bazel test 结果。
- [ ] 更新飞书多维表格中 SDK/proto 任务的 OpenSpec 证据链接。
