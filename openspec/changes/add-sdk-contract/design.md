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

## 错误模型

SDK 错误应使用结构化错误类型表达，并能映射为 UI 错误文案，避免 UI 泄漏底层实现细节。
