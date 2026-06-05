## Overview

This change turns the existing SDK adapter boundary into a verifiable runtime integration path. The UI should continue depending on `MessageRepository` and `MailRepository`; implementation details stay behind repository construction and SDK client interfaces.

## Current State

- `MockMessageRepository` and `MockMailRepository` provide UI data directly.
- `SdkMessageRepository` and `SdkMailRepository` exist, but no concrete runtime client is wired into `MainActivity`.
- `proto/message.proto`, `proto/mail.proto`, and `proto/paging.proto` define page response contracts.
- `sdk/rust/src/lib.rs` implements deterministic message/mail pagination and Rust unit tests.

## Design

### Repository Wiring

Introduce a single repository provider at the app boundary. It decides whether to use SDK-backed repositories or mock fallback repositories.

The UI screens must not import SDK DTOs, protobuf DTOs, or Rust-specific implementation details.

### SDK Client Boundary

Add concrete `MessageSdkClient` and `MailSdkClient` implementations for the current runtime.

For this project phase, the client can use a local bridge implementation if native FFI is not yet available, but it must preserve the same request/response semantics as the Rust SDK:

- `pageSize` is validated.
- `cursor == null` or empty string means first page.
- `nextCursor` is passed through unchanged.
- invalid cursor and page size errors map to a documented Kotlin error or fallback path.

### Protobuf Contract Shape

The runtime bridge must explicitly align with:

- `PageRequest`
- `PageInfo`
- `MessagePageResponse`
- `MailPageResponse`

If generated Kotlin protobuf bindings are not added in this change, the implementation must document the temporary mapping for `SDK-RT-005`; `SDK-RT-006` remains responsible for proving protobuf-shaped request/response compatibility through build/test evidence.

#### Runtime Mapping Record

Generated Kotlin protobuf bindings are not required for the first runtime bridge. Until they are introduced, concrete `MessageSdkClient` and `MailSdkClient` implementations must treat the following protobuf-shaped mapping as the contract.

Request mapping:

| Runtime call | Protobuf-shaped request | Rule |
| --- | --- | --- |
| `MessageSdkClient.getMessagePage(pageSize, cursor)` | `PageRequest.page_size` | Copy `pageSize` unchanged; SDK validation owns min/max errors. |
| `MessageSdkClient.getMessagePage(pageSize, cursor)` | `PageRequest.cursor` | Use `cursor.orEmpty()`; `null` and empty string both request the first page. |
| `MailSdkClient.getMailPage(pageSize, cursor)` | `PageRequest.page_size` | Copy `pageSize` unchanged; SDK validation owns min/max errors. |
| `MailSdkClient.getMailPage(pageSize, cursor)` | `PageRequest.cursor` | Use `cursor.orEmpty()`; non-empty cursors are opaque and passed through unchanged. |

Message response mapping:

| Protobuf-shaped field | Kotlin SDK DTO field | Domain field | Rule |
| --- | --- | --- | --- |
| `MessagePageResponse.items[]` | `SdkMessagePage.items` | `MessagePage.items` | Map each item by field name. |
| `MessagePageResponse.page_info.next_cursor` | `SdkMessagePage.nextCursor` | `MessagePage.nextCursor` | Empty string maps to `null`; non-empty values pass through unchanged. |
| `MessagePageResponse.page_info.has_more` | `SdkMessagePage.hasMore` | `MessagePage.hasMore` | Copy unchanged. |
| `MessageItem.id` | `SdkMessageItem.id` | `MessageItem.id` | Copy unchanged. |
| `MessageItem.conversation_name` | `SdkMessageItem.conversationName` | `MessageItem.conversationName` | Convert snake_case to camelCase only. |
| `MessageItem.conversation_type` | `SdkMessageItem.conversationType` | `MessageItem.conversationType` | `SINGLE`, `GROUP`, and `BOT` map one-to-one; `UNSPECIFIED` is invalid bridge data and should use the fallback/error path. |
| `MessageItem.avatar_url` | `SdkMessageItem.avatarUrl` | `MessageItem.avatarUrl` | Empty string maps to `null`; non-empty values copy unchanged. |
| `MessageItem.avatar_text` | `SdkMessageItem.avatarText` | `MessageItem.avatarText` | Copy unchanged. |
| `MessageItem.last_message_preview` | `SdkMessageItem.lastMessagePreview` | `MessageItem.lastMessagePreview` | Convert snake_case to camelCase only. |
| `MessageItem.last_message_time_millis` | `SdkMessageItem.lastMessageTimeMillis` | `MessageItem.lastMessageTimeMillis` | Copy unchanged. |
| `MessageItem.unread_count` | `SdkMessageItem.unreadCount` | `MessageItem.unreadCount` | Copy unchanged. |
| `MessageItem.is_pinned` | `SdkMessageItem.isPinned` | `MessageItem.isPinned` | Copy unchanged. |
| `MessageItem.is_muted` | `SdkMessageItem.isMuted` | `MessageItem.isMuted` | Copy unchanged. |
| `MessageItem.is_bot` | `SdkMessageItem.isBot` | `MessageItem.isBot` | Copy unchanged. |

Mail response mapping:

| Protobuf-shaped field | Kotlin SDK DTO field | Domain field | Rule |
| --- | --- | --- | --- |
| `MailPageResponse.items[]` | `SdkMailPage.items` | `MailPage.items` | Map each item by field name. |
| `MailPageResponse.page_info.next_cursor` | `SdkMailPage.nextCursor` | `MailPage.nextCursor` | Empty string maps to `null`; non-empty values pass through unchanged. |
| `MailPageResponse.page_info.has_more` | `SdkMailPage.hasMore` | `MailPage.hasMore` | Copy unchanged. |
| `MailItem.id` | `SdkMailItem.id` | `MailItem.id` | Copy unchanged. |
| `MailItem.sender` | `SdkMailItem.sender` | `MailItem.sender` | Copy unchanged. |
| `MailItem.subject` | `SdkMailItem.subject` | `MailItem.subject` | Copy unchanged. |
| `MailItem.preview` | `SdkMailItem.preview` | `MailItem.preview` | Copy unchanged. |
| `MailItem.timestamp_millis` | `SdkMailItem.timestampMillis` | `MailItem.timestampMillis` | Convert snake_case to camelCase only. |
| `MailItem.unread` | `SdkMailItem.unread` | `MailItem.unread` | Copy unchanged. |
| `MailItem.attachment_count` | `SdkMailItem.attachmentCount` | `MailItem.attachmentCount` | Copy when supplied; Rust-only mail pages default to `0`. |
| `MailItem.mail_type` | `SdkMailItem.mailType` | `MailItem.mailType` | `REMINDER`, `SYSTEM`, `COLLABORATION`, `REPORT`, and `UPDATE` map one-to-one; Rust-only mail pages default to `UPDATE`; `UNSPECIFIED` is invalid bridge data and should use the fallback/error path. |
| `MailItem.action_text` | `SdkMailItem.actionText` | `MailItem.actionText` | Empty string maps to `null`; non-empty values copy unchanged. |

Failure mapping:

- Invalid `page_size`, invalid cursor, cursor out of range, malformed enum values, and malformed response fields are bridge failures.
- A repository configured with a fallback repository delegates to that fallback on bridge failure.
- A repository without a fallback surfaces the bridge error to the caller.

### Verification

Verification should include:

- Rust SDK unit tests.
- Kotlin adapter mapping/fallback checks.
- Bazel build/test commands that cover app, proto, and SDK targets where available.
- A documented manual or scripted smoke path showing message and mail repositories can be constructed through the SDK-backed route.

## Risks

- Native Rust-to-Android FFI can increase scope. If full FFI is too large, keep the bridge interface explicit and document the remaining gap.
- Generated protobuf Kotlin targets may require additional Bazel dependencies.
- The UI must remain stable if the SDK runtime path fails.
