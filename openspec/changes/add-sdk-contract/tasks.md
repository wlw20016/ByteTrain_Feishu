# 任务：add-sdk-contract

## 1. Proto 契约

- [x] SDK-001 扩展 `message.proto`、`mail.proto` 和 `paging.proto`，与已确认的 Kotlin 领域字段保持一致。
- [x] SDK-002 定义消息和邮箱分页响应 message，或采用等价的共享分页响应策略。
- [x] 在 `design.md` 或 proto 注释中记录 cursor 语义和兼容性约定。

## 2. Rust SDK

- [x] SDK-003 实现 Rust mock 数据模型，并为消息和邮箱生成确定性的 10000 条记录。
- [x] SDK-004 实现 Rust 消息和邮箱分页 API。
- [x] 增加 Rust 单测，覆盖第一页、中间页、最后一页、非法 cursor 和 page size 边界。
  - 证据：`sdk/rust/src/lib.rs` 已新增 8 个 Rust 单测，覆盖 message 第一页、mail 中间页、message 最后一页、空 cursor、非法 cursor、越界 cursor、page size 最小/最大边界和越界错误。`cargo test` 通过，输出包含 `running 8 tests`、`8 passed; 0 failed`。

## 3. 异步与 Adapter 边界

- [x] SDK-005 定义 Kotlin 到 SDK 的异步边界和错误映射策略。
- [x] SDK-006 在 UI mock 主链路稳定后实现 SDK-backed repository adapter。
  - 证据：`features/message/data/SdkMessageRepository.kt` 和 `features/mail/data/SdkMailRepository.kt` 已实现 SDK-backed repository adapter 边界，UI 仍依赖 `MessageRepository`/`MailRepository`；`docs/ai-context/sdk-adapter-evidence.md` 已记录 SDK client、DTO 到领域模型映射和 fallback 策略；`powershell -ExecutionPolicy Bypass -File .\scripts\check-sdk-006.ps1` 通过。
- [x] 在 SDK 集成验证完成前保留 Kotlin mock repository 作为回退路径。
  - 证据：`SdkMessageRepository` 和 `SdkMailRepository` 均保留可选 `fallbackRepository`，SDK 加载失败时可委托 Kotlin mock repository；`MockMessageRepository` 和 `MockMailRepository` 仍保留在 `MESSAGE_DATA_SRCS`/`MAIL_DATA_SRCS` 中。

## 4. 证据

- [x] 记录 AI 生成的协议建议和人工取舍。
  - 证据：`openspec/prompt.md` 已记录接口草案阶段的 AI 输出摘要、人工决策和最终结果；`openspec/changes/add-sdk-contract/design.md` 已记录 proto 字段拆分、共享分页语义、cursor 透传、Kotlin adapter 边界和错误映射取舍。
- [x] 可运行时记录 `cargo test` 或 Bazel test 结果。
  - 证据：在 `sdk/rust` 下执行 `cargo test` 通过，输出显示 `running 8 tests`、`8 passed; 0 failed`，并且 doc tests 通过。
- [x] 更新飞书多维表格中 SDK/proto 任务的 OpenSpec 证据链接。




