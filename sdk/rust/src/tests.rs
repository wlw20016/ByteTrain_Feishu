use super::*;
use std::future::Future;
use std::pin::pin;
use std::sync::Arc;
use std::task::{Context, Poll, Wake, Waker};

struct NoopWake;

impl Wake for NoopWake {
    fn wake(self: Arc<Self>) {}
}

fn block_on<T>(future: impl Future<Output = T>) -> T {
    let waker = Waker::from(Arc::new(NoopWake));
    let mut context = Context::from_waker(&waker);
    let mut future = pin!(future);

    loop {
        match future.as_mut().poll(&mut context) {
            Poll::Ready(value) => return value,
            Poll::Pending => std::thread::yield_now(),
        }
    }
}

#[test]
fn message_first_page_uses_empty_cursor() {
    let sdk = MockFeedSdk::with_total_count(45);

    let page = sdk.get_message_page(20, None).expect("first page");

    assert_eq!(page.items.len(), 20);
    assert_eq!(
        page.items.first().map(|item| item.id.as_str()),
        Some("message-1")
    );
    assert_eq!(
        page.items.last().map(|item| item.id.as_str()),
        Some("message-20")
    );
    assert_eq!(page.next_cursor.as_deref(), Some("20"));
    assert!(page.has_more);
}

#[test]
fn mail_middle_page_uses_cursor_as_start_index() {
    let sdk = MockFeedSdk::with_total_count(100);

    let page = sdk.get_mail_page(20, Some("40")).expect("middle page");

    assert_eq!(page.items.len(), 20);
    assert_eq!(
        page.items.first().map(|item| item.id.as_str()),
        Some("mail-41")
    );
    assert_eq!(
        page.items.last().map(|item| item.id.as_str()),
        Some("mail-60")
    );
    assert_eq!(page.next_cursor.as_deref(), Some("60"));
    assert!(page.has_more);
}

#[test]
fn message_last_page_has_no_next_cursor() {
    let sdk = MockFeedSdk::with_total_count(45);

    let page = sdk.get_message_page(20, Some("40")).expect("last page");

    assert_eq!(page.items.len(), 5);
    assert_eq!(
        page.items.first().map(|item| item.id.as_str()),
        Some("message-41")
    );
    assert_eq!(
        page.items.last().map(|item| item.id.as_str()),
        Some("message-45")
    );
    assert_eq!(page.next_cursor, None);
    assert!(!page.has_more);
}

#[test]
fn empty_cursor_is_equivalent_to_first_page() {
    let sdk = MockFeedSdk::with_total_count(12);

    let page = sdk.get_mail_page(5, Some("")).expect("empty cursor");

    assert_eq!(page.items.len(), 5);
    assert_eq!(
        page.items.first().map(|item| item.id.as_str()),
        Some("mail-1")
    );
    assert_eq!(page.next_cursor.as_deref(), Some("5"));
    assert!(page.has_more);
}

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

#[test]
fn out_of_range_cursor_returns_structured_error() {
    let sdk = MockFeedSdk::with_total_count(45);

    let error = sdk
        .get_mail_page(20, Some("46"))
        .expect_err("cursor out of range");

    assert_eq!(
        error,
        SdkError::CursorOutOfRange {
            cursor: "46".to_owned(),
            total_count: 45
        }
    );
}

#[test]
fn page_size_accepts_min_and_max_boundaries() {
    let sdk = MockFeedSdk::with_total_count(250);

    let min_page = sdk
        .get_message_page(MIN_PAGE_SIZE, None)
        .expect("min page size");
    let max_page = sdk
        .get_mail_page(MAX_PAGE_SIZE, None)
        .expect("max page size");

    assert_eq!(min_page.items.len(), MIN_PAGE_SIZE);
    assert_eq!(min_page.next_cursor.as_deref(), Some("1"));
    assert!(min_page.has_more);
    assert_eq!(max_page.items.len(), MAX_PAGE_SIZE);
    assert_eq!(max_page.next_cursor.as_deref(), Some("200"));
    assert!(max_page.has_more);
}

#[test]
fn page_size_rejects_values_outside_boundaries() {
    let sdk = MockFeedSdk::with_total_count(45);

    assert_eq!(
        sdk.get_message_page(0, None).expect_err("zero page size"),
        SdkError::InvalidPageSize {
            page_size: 0,
            min: MIN_PAGE_SIZE,
            max: MAX_PAGE_SIZE
        }
    );
    assert_eq!(
        sdk.get_mail_page(MAX_PAGE_SIZE + 1, None)
            .expect_err("too large page size"),
        SdkError::InvalidPageSize {
            page_size: MAX_PAGE_SIZE + 1,
            min: MIN_PAGE_SIZE,
            max: MAX_PAGE_SIZE
        }
    );
}

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

    assert_eq!(
        first.items.first().map(|item| item.id.as_str()),
        Some("message-1")
    );
    assert_eq!(first.next_cursor.as_deref(), Some("20"));
    assert!(first.has_more);
    assert_eq!(
        next.items.first().map(|item| item.id.as_str()),
        Some("message-21")
    );
    assert_eq!(next.next_cursor.as_deref(), Some("40"));
    assert!(next.has_more);
}

#[test]
fn async_mail_read_uses_cursor_as_start_index() {
    let sdk = MockFeedSdk::with_total_count(80);

    let page = block_on(sdk.read_mail_page(PageRequest {
        page_size: 15,
        cursor: Some("30".to_owned()),
    }))
    .expect("async mail page");

    assert_eq!(page.items.len(), 15);
    assert_eq!(
        page.items.first().map(|item| item.id.as_str()),
        Some("mail-31")
    );
    assert_eq!(page.next_cursor.as_deref(), Some("45"));
    assert!(page.has_more);
}

#[test]
fn async_read_returns_structured_request_errors() {
    let sdk = MockFeedSdk::with_total_count(10);

    assert_eq!(
        block_on(sdk.read_message_page(PageRequest {
            page_size: 0,
            cursor: None,
        }))
        .expect_err("invalid page size"),
        SdkError::InvalidPageSize {
            page_size: 0,
            min: MIN_PAGE_SIZE,
            max: MAX_PAGE_SIZE
        }
    );
    assert_eq!(
        block_on(sdk.read_message_page(PageRequest {
            page_size: 5,
            cursor: Some("bad-cursor".to_owned()),
        }))
        .expect_err("invalid cursor"),
        SdkError::InvalidCursor {
            cursor: "bad-cursor".to_owned()
        }
    );
    assert_eq!(
        block_on(sdk.read_mail_page(PageRequest {
            page_size: 5,
            cursor: Some("11".to_owned()),
        }))
        .expect_err("out of range cursor"),
        SdkError::CursorOutOfRange {
            cursor: "11".to_owned(),
            total_count: 10
        }
    );
}

#[test]
fn protobuf_page_request_round_trip_preserves_fields() {
    let request = PageRequest {
        page_size: 25,
        cursor: Some("50".to_owned()),
    };

    let encoded = encode_page_request(&request).expect("encode request");
    let decoded = decode_page_request(&encoded).expect("decode request");

    assert_eq!(decoded, request);
}

#[test]
fn protobuf_message_and_mail_responses_round_trip() {
    let sdk = MockFeedSdk::with_total_count(12);
    let message_page = sdk.get_message_page(4, Some("4")).expect("message page");
    let mail_page = sdk.get_mail_page(4, Some("4")).expect("mail page");

    let decoded_message_page =
        decode_message_page_response(&encode_message_page_response(&message_page).unwrap())
            .expect("decode message page");
    let decoded_mail_page =
        decode_mail_page_response(&encode_mail_page_response(&mail_page).unwrap())
            .expect("decode mail page");

    assert_eq!(decoded_message_page, message_page);
    assert_eq!(decoded_mail_page, mail_page);
    assert_eq!(decoded_mail_page.items[1].attachment_count, 1);
    assert_eq!(decoded_mail_page.items[1].mail_type, MailType::Reminder);
    assert_eq!(
        decoded_mail_page.items[1].action_text.as_deref(),
        Some("View")
    );
}

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

#[test]
fn bridge_message_bytes_returns_first_and_next_pages() {
    let first_request = encode_page_request(&PageRequest {
        page_size: 20,
        cursor: None,
    })
    .expect("encode first request");
    let first = decode_message_page_response(
        &read_message_page_response_bytes(&first_request).expect("bridge first page"),
    )
    .expect("decode first bridge response");

    let next_request = encode_page_request(&PageRequest {
        page_size: 20,
        cursor: first.next_cursor.clone(),
    })
    .expect("encode next request");
    let next = decode_message_page_response(
        &read_message_page_response_bytes(&next_request).expect("bridge next page"),
    )
    .expect("decode next bridge response");

    assert_eq!(first.items.len(), 20);
    assert_eq!(first.items.first().map(|item| item.id.as_str()), Some("message-1"));
    assert_eq!(first.next_cursor.as_deref(), Some("20"));
    assert!(first.has_more);
    assert_eq!(next.items.first().map(|item| item.id.as_str()), Some("message-21"));
    assert_eq!(next.next_cursor.as_deref(), Some("40"));
}

#[test]
fn bridge_mail_bytes_returns_first_and_next_pages() {
    let first_request = encode_page_request(&PageRequest {
        page_size: 15,
        cursor: None,
    })
    .expect("encode first request");
    let first =
        decode_mail_page_response(&read_mail_page_response_bytes(&first_request).expect("bridge first page"))
            .expect("decode first bridge response");

    let next_request = encode_page_request(&PageRequest {
        page_size: 15,
        cursor: first.next_cursor.clone(),
    })
    .expect("encode next request");
    let next =
        decode_mail_page_response(&read_mail_page_response_bytes(&next_request).expect("bridge next page"))
            .expect("decode next bridge response");

    assert_eq!(first.items.len(), 15);
    assert_eq!(first.items.first().map(|item| item.id.as_str()), Some("mail-1"));
    assert_eq!(first.next_cursor.as_deref(), Some("15"));
    assert!(first.has_more);
    assert_eq!(next.items.first().map(|item| item.id.as_str()), Some("mail-16"));
    assert_eq!(next.next_cursor.as_deref(), Some("30"));
}

#[test]
fn bridge_bytes_errors_are_structured() {
    let invalid_page_size = encode_page_request(&PageRequest {
        page_size: 0,
        cursor: None,
    })
    .expect("encode invalid page size");
    let invalid_cursor = encode_page_request(&PageRequest {
        page_size: 20,
        cursor: Some("bad".to_owned()),
    })
    .expect("encode invalid cursor");
    let out_of_range = encode_page_request(&PageRequest {
        page_size: 20,
        cursor: Some("10001".to_owned()),
    })
    .expect("encode out-of-range cursor");

    assert_eq!(
        read_message_page_response_bytes(&invalid_page_size)
            .expect_err("invalid page size")
            .code,
        BridgeErrorCode::InvalidPageSize
    );
    assert_eq!(
        read_message_page_response_bytes(&invalid_cursor)
            .expect_err("invalid cursor")
            .code,
        BridgeErrorCode::InvalidCursor
    );
    assert_eq!(
        read_mail_page_response_bytes(&out_of_range)
            .expect_err("out-of-range cursor")
            .code,
        BridgeErrorCode::CursorOutOfRange
    );
    assert_eq!(
        read_mail_page_response_bytes(&[0x08, 0x80])
            .expect_err("malformed protobuf")
            .code,
        BridgeErrorCode::ProtobufDecode
    );
}
