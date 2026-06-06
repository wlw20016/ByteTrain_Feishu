# Rust SDK async/protobuf contract draft

This note is an AI-generated implementation check and contract draft for the
current Rust SDK boundary. It is grounded in `sdk/rust/src/lib.rs`,
`proto/*.proto`, and the verified Rust tests.

## Implementation status

The project does implement a Rust SDK boundary for data parsing, async page
reads, and protobuf-shaped communication.

Implemented:

- Data parsing and deterministic page reads in `MockFeedSdk`.
- Sync helpers: `get_message_page` and `get_mail_page`.
- Async helpers: `read_message_page` and `read_mail_page`.
- Protobuf wire helpers for `PageRequest`, `MessagePageResponse`, and
  `MailPageResponse`.
- Structured SDK errors for request validation, cursor failures, protobuf
  encode/decode failures, and async read failures.
- Rust unit tests for pagination, async reads, protobuf round trips, and decode
  failures.
- Bazel target `//sdk/rust:bytetrain_feed_sdk_test`.

Current limitations:

- The async implementation is local and deterministic. It does not yet perform
  network I/O.
- Android runtime is not connected to native Rust FFI yet.
- Rust protobuf support is implemented through SDK-local proto3 wire helpers,
  not generated `prost` or `tonic` bindings.

## Protocol draft

The stable proto package is `bytetrain.feed`.

### PageRequest

Client request for one page.

```proto
message PageRequest {
  int32 page_size = 1;
  string cursor = 2;
}
```

Rules:

- `page_size` must be in `1..=200`.
- Empty or omitted `cursor` requests the first page.
- Non-empty `cursor` is opaque to callers.
- Current mock SDK cursors are zero-based decimal start indexes.
- Clients must pass `next_cursor` back unchanged.

### PageInfo

Shared response metadata.

```proto
message PageInfo {
  string next_cursor = 1;
  bool has_more = 2;
}
```

Rules:

- `has_more == true` means `next_cursor` can be used to request the next page.
- `has_more == false` means there are no more items.
- `next_cursor` should be omitted or empty when `has_more == false`.

### MessagePageResponse

```proto
message MessagePageResponse {
  repeated MessageItem items = 1;
  PageInfo page_info = 2;
}
```

`MessageItem` fields are mapped one-to-one to Rust `MessageItem`: id,
conversation name/type, avatar fields, preview, timestamp, unread count,
pinned/muted state, and bot flag.

### MailPageResponse

```proto
message MailPageResponse {
  repeated MailItem items = 1;
  PageInfo page_info = 2;
}
```

`MailItem` fields are mapped one-to-one to Rust `MailItem`: id, sender,
subject, preview, timestamp, unread flag, attachment count, mail type, and
optional action text.

### Byte-level SDK helpers

The SDK currently exposes these protobuf boundary helpers:

```rust
pub fn encode_page_request(request: &PageRequest) -> SdkResult<Vec<u8>>;
pub fn decode_page_request(bytes: &[u8]) -> SdkResult<PageRequest>;

pub fn encode_message_page_response(response: &MessagePageResponse) -> SdkResult<Vec<u8>>;
pub fn decode_message_page_response(bytes: &[u8]) -> SdkResult<MessagePageResponse>;

pub fn encode_mail_page_response(response: &MailPageResponse) -> SdkResult<Vec<u8>>;
pub fn decode_mail_page_response(bytes: &[u8]) -> SdkResult<MailPageResponse>;
```

A future transport can wrap this as:

```text
read_message_page(PageRequest bytes) -> MessagePageResponse bytes
read_mail_page(PageRequest bytes) -> MailPageResponse bytes
```

## Error model

Rust result alias:

```rust
pub type SdkResult<T> = Result<T, SdkError>;
```

Structured errors:

```rust
pub enum SdkError {
    InvalidPageSize {
        page_size: usize,
        min: usize,
        max: usize,
    },
    InvalidCursor {
        cursor: String,
    },
    CursorOutOfRange {
        cursor: String,
        total_count: usize,
    },
    ProtobufDecode {
        target: ProtobufTarget,
        failure: ProtobufFailure,
    },
    ProtobufEncode {
        target: ProtobufTarget,
        failure: ProtobufFailure,
    },
    AsyncRead {
        operation: &'static str,
        reason: String,
    },
}
```

Protobuf failures:

```rust
pub enum ProtobufFailure {
    InvalidWireType { field: u32, expected: u8, actual: u8 },
    InvalidFieldNumber,
    InvalidVarint,
    InvalidLength,
    InvalidUtf8,
    InvalidEnum { field: u32, value: i32 },
    NumericOverflow { field: u32 },
}
```

Recommended mapping for Android or transport callers:

| SDK error | Caller behavior |
| --- | --- |
| `InvalidPageSize` | Reject caller input or fall back to default page size. |
| `InvalidCursor` | Treat the cursor as stale or corrupted; reload first page or fallback. |
| `CursorOutOfRange` | Treat the cursor as stale; reload first page or fallback. |
| `ProtobufDecode` | Return transport/protocol failure and log `target` plus `failure`. |
| `ProtobufEncode` | Return internal protocol failure and log `target` plus `failure`. |
| `AsyncRead` | Return data-source failure; fallback remains acceptable in the app phase. |

## Async interface example

```rust
use bytetrain_feed_sdk::{MockFeedSdk, PageRequest, SdkResult};

async fn load_two_message_pages() -> SdkResult<()> {
    let sdk = MockFeedSdk::with_total_count(45);

    let first = sdk
        .read_message_page(PageRequest {
            page_size: 20,
            cursor: None,
        })
        .await?;

    let second = sdk
        .read_message_page(PageRequest {
            page_size: 20,
            cursor: first.next_cursor.clone(),
        })
        .await?;

    assert_eq!(first.items[0].id, "message-1");
    assert_eq!(second.items[0].id, "message-21");

    Ok(())
}
```

```rust
use bytetrain_feed_sdk::{read_mail_page, PageRequest, SdkResult};

async fn load_mail_page(cursor: Option<String>) -> SdkResult<()> {
    let page = read_mail_page(PageRequest {
        page_size: 15,
        cursor,
    })
    .await?;

    if page.has_more {
        let next_cursor = page.next_cursor;
        assert!(next_cursor.is_some());
    }

    Ok(())
}
```

## Protobuf example

```rust
use bytetrain_feed_sdk::{
    decode_message_page_response, encode_message_page_response, MockFeedSdk, SdkResult,
};

fn message_response_round_trip() -> SdkResult<()> {
    let sdk = MockFeedSdk::with_total_count(12);
    let page = sdk.get_message_page(4, Some("4"))?;

    let bytes = encode_message_page_response(&page)?;
    let decoded = decode_message_page_response(&bytes)?;

    assert_eq!(decoded, page);
    Ok(())
}
```

## Test samples

Async first and next page:

```rust
#[test]
fn async_message_read_returns_first_and_next_pages() {
    let sdk = MockFeedSdk::with_total_count(45);

    let first = block_on(sdk.read_message_page(PageRequest {
        page_size: 20,
        cursor: None,
    }))
    .expect("async first page");

    let next = block_on(sdk.read_message_page(PageRequest {
        page_size: 20,
        cursor: first.next_cursor.clone(),
    }))
    .expect("async next page");

    assert_eq!(first.items[0].id, "message-1");
    assert_eq!(next.items[0].id, "message-21");
}
```

Structured request error:

```rust
#[test]
fn invalid_cursor_returns_structured_error() {
    let sdk = MockFeedSdk::with_total_count(45);

    let error = sdk
        .get_message_page(20, Some("not-a-number"))
        .expect_err("invalid cursor");

    assert_eq!(
        error,
        SdkError::InvalidCursor {
            cursor: "not-a-number".to_owned()
        }
    );
}
```

Protobuf decode failure:

```rust
#[test]
fn protobuf_decode_failure_returns_structured_error() {
    let error = decode_page_request(&[0x08, 0x80]).expect_err("truncated varint");

    assert_eq!(
        error,
        SdkError::ProtobufDecode {
            target: ProtobufTarget::PageRequest,
            failure: ProtobufFailure::InvalidVarint
        }
    );
}
```

## Verification commands

Verified on 2026-06-06:

```powershell
cargo test --manifest-path sdk/rust/Cargo.toml
bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4
```

Results:

- `cargo test`: 14 passed, 0 failed.
- Bazel: `//sdk/rust:bytetrain_feed_sdk_test` passed.
