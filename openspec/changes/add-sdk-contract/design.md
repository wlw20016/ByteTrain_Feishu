# Design: add-sdk-contract

## Contract First

Business models remain separate:

- Message: conversation-focused fields.
- Mail: mail-card-focused fields.

Paging semantics are shared:

- `page_size`: requested page size.
- `cursor`: empty or absent means first page.
- `next_cursor`: absent or empty with `has_more = false` means no more records.
- `has_more`: whether another page can be requested.

## Proto Plan

Update proto files to match the agreed business model fields:

- `message.proto`: message item and message page response.
- `mail.proto`: mail item and mail page response.
- `paging.proto`: common page request and page info.

## Rust SDK Plan

Rust SDK exposes a small stable mock service boundary:

- `get_message_page(page_size, cursor) -> SdkResult<Page<MessageItem>>`
- `get_mail_page(page_size, cursor) -> SdkResult<Page<MailItem>>`

The first implementation can generate deterministic mock data in memory. The SDK shall include tests for pagination and cursor handling.

## Kotlin Adapter Plan

The UI keeps using repository interfaces. SDK integration happens behind adapters:

- `SdkMessageRepository : MessageRepository`
- `SdkMailRepository : MailRepository`

This keeps UI code independent from SDK transport details.

## Error Model

Errors should be represented with a structured SDK error type that can be mapped to UI error messages without leaking low-level implementation details.
