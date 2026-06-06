# SDK adapter evidence

This document records the final `connect-sdk-protobuf-runtime` adapter path after the UI was switched from direct mock repositories to SDK-backed repository construction with mock fallback.

## Runtime path

The app runtime now constructs message and mail repositories through `AppRepositoryProvider`:

```text
MainActivity
  -> AppRepositoryProvider()
  -> SdkMessageRepository(RuntimeMessageSdkClient(), MockMessageRepository())
  -> SdkMailRepository(RuntimeMailSdkClient(), MockMailRepository())
  -> UI-facing MessageRepository / MailRepository interfaces
```

`MainActivity` keeps depending on `MessageRepository` and `MailRepository` behavior through `loadPage(pageSize, cursor)`. UI screens do not import SDK DTOs, protobuf DTOs, Rust SDK types, or runtime bridge implementation details.

## Repository provider

`AppRepositoryProvider` is the app composition root for data repositories.

- Default behavior keeps `sdkRuntimeEnabled = true`.
- Message construction returns `SdkMessageRepository(RuntimeMessageSdkClient(), fallback)`.
- Mail construction returns `SdkMailRepository(RuntimeMailSdkClient(), fallback)`.
- The fallback path is always constructed from `MockMessageRepository` / `MockMailRepository`.
- If SDK runtime construction fails, the provider returns the mock fallback repository.
- If SDK loading fails after construction, `SdkMessageRepository` / `SdkMailRepository` delegate the original `pageSize` and `cursor` request to the configured fallback repository.

## SDK client boundary

The runtime bridge remains behind explicit SDK client interfaces:

- `MessageSdkClient.getMessagePage(pageSize, cursor)`
- `MailSdkClient.getMailPage(pageSize, cursor)`

The current concrete clients are:

- `RuntimeMessageSdkClient`
- `RuntimeMailSdkClient`

These clients provide deterministic local runtime data with the same pagination semantics as the Rust SDK:

- `pageSize` must be in `1..200`.
- `cursor == null` or `cursor == ""` requests the first page.
- Non-empty cursors are parsed as zero-based start indexes.
- Invalid cursors, out-of-range cursors, and invalid page sizes fail the bridge request so the repository fallback strategy can run.
- `nextCursor` is set only when `hasMore == true`; otherwise it is `null`.

## Protobuf-shaped contract

Generated Kotlin protobuf bindings are still not part of this runtime path. Instead, the runtime bridge uses the protobuf-shaped mapping documented in `openspec/changes/connect-sdk-protobuf-runtime/design.md`.

The mapping aligns the runtime request/response shape with:

- `PageRequest.page_size`
- `PageRequest.cursor`
- `PageInfo.next_cursor`
- `PageInfo.has_more`
- `MessagePageResponse.items`
- `MailPageResponse.items`

Message fields are mapped one-to-one from SDK DTOs to `MessageItem`, including conversation type, avatar fields, preview timestamp, unread count, pinned/muted state, and bot state.

Mail fields are mapped one-to-one from SDK DTOs to `MailItem`. Rust mail pages now include `attachment_count`, `mail_type`, and `action_text` so the Rust domain model matches `proto/mail.proto`.

## Rust async/protobuf SDK boundary

The Rust SDK exposes async page reads while preserving the existing synchronous helpers:

```rust
pub async fn read_message_page(request: PageRequest) -> SdkResult<MessagePageResponse>;
pub async fn read_mail_page(request: PageRequest) -> SdkResult<MailPageResponse>;
```

`MockFeedSdk::read_message_page` and `MockFeedSdk::read_mail_page` use the same deterministic pagination semantics as `get_message_page` and `get_mail_page`: `page_size` must be `1..=200`, `None` or empty cursor requests the first page, and non-empty cursors are zero-based item offsets returned by the previous page.

Rust request/response mapping:

| Proto field | Rust SDK field |
| --- | --- |
| `PageRequest.page_size` | `PageRequest.page_size` |
| `PageRequest.cursor` | `PageRequest.cursor` |
| `PageInfo.next_cursor` | `Page<T>.next_cursor` |
| `PageInfo.has_more` | `Page<T>.has_more` |
| `MessagePageResponse.items` | `MessagePageResponse.items` |
| `MailPageResponse.items` | `MailPageResponse.items` |

The SDK provides tested protobuf helpers for the current proto3 boundary:

- `encode_page_request` / `decode_page_request`
- `encode_message_page_response` / `decode_message_page_response`
- `encode_mail_page_response` / `decode_mail_page_response`

Structured error coverage:

- `InvalidPageSize`
- `InvalidCursor`
- `CursorOutOfRange`
- `ProtobufDecode { target, failure }`
- `ProtobufEncode { target, failure }`
- `AsyncRead { operation, reason }`

The current async implementation is local and deterministic; no native Android FFI or real network transport is attached yet. Protobuf support is implemented as SDK-local wire encoding/decoding helpers for the fields used by `proto/paging.proto`, `proto/message.proto`, and `proto/mail.proto`; generated Rust protobuf bindings are still a future integration option.

## Verification evidence

Focused Kotlin tests:

- `.\gradlew.bat :app:testDebugUnitTest`
- Message suite: first page, next page, invalid cursor fallback, and key field mapping.
- Mail suite: first page, next page, invalid cursor fallback, and key field mapping.
- Result recorded in OpenSpec tasks: 6 focused SDK-backed repository tests passed.

Rust SDK tests:

- `cargo test --manifest-path sdk/rust/Cargo.toml`
- Result recorded for `complete-rust-sdk-async-protobuf`: 14 unit tests passed, doc-tests had 0 tests.

Bazel proto/app/SDK evidence:

- `bazel.exe --batch build //proto:feed_proto //proto:paging_proto //proto:message_proto //proto:mail_proto --verbose_failures`
- `bazel.exe --batch test //sdk/rust:bytetrain_feed_sdk_test --verbose_failures --test_output=errors`
- `bazel.exe --batch build //features/message:data //features/mail:data --verbose_failures`
- `bazel.exe --batch build //app:app_lib --verbose_failures`
- `bazel.exe --batch build //app:app --verbose_failures --jobs=1 --local_cpu_resources=1`

The app binary retry produced:

- `bazel-bin/app/app_deploy.jar`
- `bazel-bin/app/app_unsigned.apk`
- `bazel-bin/app/app.apk`

Bazel query confirmed the repository currently has one test rule:

- `//sdk/rust:bytetrain_feed_sdk_test`

## Remaining limitations

- The Android runtime bridge is not native Rust FFI. It is a Kotlin runtime implementation that preserves Rust SDK pagination semantics behind the SDK client interfaces.
- Generated Kotlin protobuf bindings are not wired into the runtime path yet. The protobuf contract is validated through proto BUILD targets and documented shape mapping.
- `RuntimeMessageSdkClient` and `RuntimeMailSdkClient` use deterministic local data for this project phase; real transport can be attached behind `MessageSdkClient` and `MailSdkClient` without changing UI code.
- Mock fallback remains intentionally enabled to preserve UI availability while native SDK/protobuf transport is incomplete.
- Bazel on this Windows environment needs the local Bazel binary path from the existing Bazelisk cache because plain `bazelisk` attempts to resolve `latest` over the network when `.bazelversion` is absent.
- Full `//app:app` Bazel build may require low concurrency on this machine. The successful retry used `--jobs=1 --local_cpu_resources=1` after the default attempt hit a Windows page-file/native-memory allocation failure in Android resource linking.
- Bazel output still includes non-blocking warnings from the non-ASCII host name Java log handler, MSVC code page warnings, and deprecated `--local_cpu_resources`.
