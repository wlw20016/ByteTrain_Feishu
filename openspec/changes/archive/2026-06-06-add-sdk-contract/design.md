# Design: add-sdk-contract

## 契约先行

业务模型保持分离：

- 消息：以会话字段为核心。
- 邮箱：以邮件卡片字段为核心。

分页语义共享：

- `page_size`：请求页大小。
- `cursor`：为空或缺省表示第一页。
- `next_cursor`：为空且 `has_more = false` 表示没有更多数据。
- `has_more`：是否还可以继续请求下一页。

## Proto 方案

更新 proto 文件，使其匹配已确认的业务模型字段：

- `message.proto`：消息 item 和消息分页响应。
- `mail.proto`：邮箱 item 和邮箱分页响应。
- `paging.proto`：通用分页请求和分页信息。

## Rust SDK 方案

Rust SDK 暴露小而稳定的 mock 服务边界：

- `get_message_page(page_size, cursor) -> SdkResult<Page<MessageItem>>`
- `get_mail_page(page_size, cursor) -> SdkResult<Page<MailItem>>`

第一版实现可以在内存中生成确定性的 mock 数据。SDK 需要包含分页和 cursor 处理测试。

## Kotlin Adapter 方案

UI 继续依赖 repository 接口。SDK 集成隐藏在 adapter 后面：

- `SdkMessageRepository : MessageRepository`
- `SdkMailRepository : MailRepository`

这样 UI 代码不依赖 SDK 传输细节。

异步边界约定：

- Kotlin adapter 对外只暴露既有 `suspend fun loadPage(pageSize, cursor)` repository 接口。
- adapter 在 `Dispatchers.IO` 或等价后台执行上下文调用 SDK，避免 UI 线程直接阻塞。
- SDK 返回的 protobuf/Rust DTO 在 adapter 内映射为 Kotlin 领域模型，UI 不持有 SDK 内部类型。
- `cursor` 由 SDK 签发，Kotlin adapter 仅透传 `nextCursor`，不解析、不拼接、不自行生成 cursor。
- SDK-backed repository 接入完成前，Kotlin mock repository 继续作为可切换回退路径。

## 错误模型

SDK 错误应使用结构化错误类型表达，并能映射为 UI 错误文案，避免 UI 泄漏底层实现细节。

错误映射策略：

- `InvalidPageSize` 映射为 repository 层的分页参数错误，可在 UI 显示为“分页参数无效”。
- `InvalidCursor` 和 `CursorOutOfRange` 映射为分页状态错误，adapter 可清空 cursor 后重新请求第一页或向 UI 返回可恢复错误。
- 网络、FFI 或序列化错误在 SDK 集成阶段统一包装为 repository 层数据源错误。
- UI 文案不包含底层 cursor、FFI、protobuf 或 Rust 错误细节；详细错误仅用于日志和调试证据。
