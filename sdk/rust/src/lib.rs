use std::fmt;

pub const DEFAULT_TOTAL_COUNT: usize = 10_000;
pub const MIN_PAGE_SIZE: usize = 1;
pub const MAX_PAGE_SIZE: usize = 200;

const DEFAULT_BASE_TIME_MILLIS: i64 = 1_717_200_000_000;
const MESSAGE_INTERVAL_MILLIS: i64 = 60_000;
const MAIL_INTERVAL_MILLIS: i64 = 300_000;

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct Page<T> {
    pub items: Vec<T>,
    pub next_cursor: Option<String>,
    pub has_more: bool,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct PageRequest {
    pub page_size: usize,
    pub cursor: Option<String>,
}

pub type MessagePageResponse = Page<MessageItem>;
pub type MailPageResponse = Page<MailItem>;

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ConversationType {
    Single,
    Group,
    Bot,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct MessageItem {
    pub id: String,
    pub conversation_name: String,
    pub conversation_type: ConversationType,
    pub avatar_url: Option<String>,
    pub avatar_text: String,
    pub last_message_preview: String,
    pub last_message_time_millis: i64,
    pub unread_count: i32,
    pub is_pinned: bool,
    pub is_muted: bool,
    pub is_bot: bool,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct MailItem {
    pub id: String,
    pub sender: String,
    pub subject: String,
    pub preview: String,
    pub timestamp_millis: i64,
    pub unread: bool,
    pub attachment_count: i32,
    pub mail_type: MailType,
    pub action_text: Option<String>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum MailType {
    Reminder,
    System,
    Collaboration,
    Report,
    Update,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ProtobufTarget {
    PageRequest,
    PageInfo,
    MessageItem,
    MessagePageResponse,
    MailItem,
    MailPageResponse,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ProtobufFailure {
    InvalidWireType {
        field: u32,
        expected: u8,
        actual: u8,
    },
    InvalidFieldNumber,
    InvalidVarint,
    InvalidLength,
    InvalidUtf8,
    InvalidEnum {
        field: u32,
        value: i32,
    },
    NumericOverflow {
        field: u32,
    },
}

#[derive(Debug, Clone, PartialEq, Eq)]
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

impl fmt::Display for SdkError {
    fn fmt(&self, formatter: &mut fmt::Formatter<'_>) -> fmt::Result {
        match self {
            SdkError::InvalidPageSize {
                page_size,
                min,
                max,
            } => write!(
                formatter,
                "invalid page_size {page_size}; expected value in {min}..={max}"
            ),
            SdkError::InvalidCursor { cursor } => {
                write!(formatter, "invalid cursor '{cursor}'")
            }
            SdkError::CursorOutOfRange {
                cursor,
                total_count,
            } => write!(
                formatter,
                "cursor '{cursor}' is outside the item range 0..={total_count}"
            ),
            SdkError::ProtobufDecode { target, failure } => {
                write!(formatter, "failed to decode {target:?}: {failure:?}")
            }
            SdkError::ProtobufEncode { target, failure } => {
                write!(formatter, "failed to encode {target:?}: {failure:?}")
            }
            SdkError::AsyncRead { operation, reason } => {
                write!(formatter, "async read {operation} failed: {reason}")
            }
        }
    }
}

impl std::error::Error for SdkError {}

pub type SdkResult<T> = Result<T, SdkError>;

#[derive(Debug, Clone)]
pub struct MockFeedSdk {
    messages: Vec<MessageItem>,
    mails: Vec<MailItem>,
}

impl Default for MockFeedSdk {
    fn default() -> Self {
        Self::new()
    }
}

impl MockFeedSdk {
    pub fn new() -> Self {
        Self::with_total_count(DEFAULT_TOTAL_COUNT)
    }

    pub fn with_total_count(total_count: usize) -> Self {
        Self {
            messages: generate_message_items(total_count),
            mails: generate_mail_items(total_count),
        }
    }

    pub fn get_message_page(
        &self,
        page_size: usize,
        cursor: Option<&str>,
    ) -> SdkResult<Page<MessageItem>> {
        paginate(&self.messages, page_size, cursor)
    }

    pub fn get_mail_page(
        &self,
        page_size: usize,
        cursor: Option<&str>,
    ) -> SdkResult<Page<MailItem>> {
        paginate(&self.mails, page_size, cursor)
    }

    pub async fn read_message_page(&self, request: PageRequest) -> SdkResult<MessagePageResponse> {
        self.get_message_page(request.page_size, request.cursor.as_deref())
    }

    pub async fn read_mail_page(&self, request: PageRequest) -> SdkResult<MailPageResponse> {
        self.get_mail_page(request.page_size, request.cursor.as_deref())
    }

    pub fn message_count(&self) -> usize {
        self.messages.len()
    }

    pub fn mail_count(&self) -> usize {
        self.mails.len()
    }
}

pub fn get_message_page(page_size: usize, cursor: Option<&str>) -> SdkResult<Page<MessageItem>> {
    MockFeedSdk::default().get_message_page(page_size, cursor)
}

pub fn get_mail_page(page_size: usize, cursor: Option<&str>) -> SdkResult<Page<MailItem>> {
    MockFeedSdk::default().get_mail_page(page_size, cursor)
}

pub async fn read_message_page(request: PageRequest) -> SdkResult<MessagePageResponse> {
    MockFeedSdk::default().read_message_page(request).await
}

pub async fn read_mail_page(request: PageRequest) -> SdkResult<MailPageResponse> {
    MockFeedSdk::default().read_mail_page(request).await
}

pub fn generate_message_items(total_count: usize) -> Vec<MessageItem> {
    (0..total_count).map(create_message).collect()
}

pub fn generate_mail_items(total_count: usize) -> Vec<MailItem> {
    (0..total_count).map(create_mail).collect()
}

fn paginate<T: Clone>(items: &[T], page_size: usize, cursor: Option<&str>) -> SdkResult<Page<T>> {
    validate_page_size(page_size)?;

    let start_index = parse_cursor(cursor, items.len())?;
    let end_index = start_index.saturating_add(page_size).min(items.len());
    let page_items = items[start_index..end_index].to_vec();
    let has_more = end_index < items.len();

    Ok(Page {
        items: page_items,
        next_cursor: has_more.then(|| end_index.to_string()),
        has_more,
    })
}

fn validate_page_size(page_size: usize) -> SdkResult<()> {
    if (MIN_PAGE_SIZE..=MAX_PAGE_SIZE).contains(&page_size) {
        Ok(())
    } else {
        Err(SdkError::InvalidPageSize {
            page_size,
            min: MIN_PAGE_SIZE,
            max: MAX_PAGE_SIZE,
        })
    }
}

fn parse_cursor(cursor: Option<&str>, total_count: usize) -> SdkResult<usize> {
    match cursor {
        None | Some("") => Ok(0),
        Some(value) => {
            let start_index = value
                .parse::<usize>()
                .map_err(|_| SdkError::InvalidCursor {
                    cursor: value.to_owned(),
                })?;

            if start_index <= total_count {
                Ok(start_index)
            } else {
                Err(SdkError::CursorOutOfRange {
                    cursor: value.to_owned(),
                    total_count,
                })
            }
        }
    }
}

pub fn encode_page_request(request: &PageRequest) -> SdkResult<Vec<u8>> {
    let mut output = Vec::new();
    encode_int32_field(
        1,
        usize_to_i32(request.page_size, ProtobufTarget::PageRequest, 1)?,
        &mut output,
    );
    if let Some(cursor) = request.cursor.as_deref() {
        encode_string_field(2, cursor, &mut output);
    }
    Ok(output)
}

pub fn decode_page_request(bytes: &[u8]) -> SdkResult<PageRequest> {
    let mut page_size = 0usize;
    let mut cursor = None;
    let mut decoder = ProtoDecoder::new(bytes, ProtobufTarget::PageRequest);

    while let Some(field) = decoder.next_field()? {
        match field.number {
            1 => {
                field.expect_wire_type(WIRE_VARINT)?;
                page_size = i32_to_usize(field.read_i32()?, ProtobufTarget::PageRequest, 1)?;
            }
            2 => {
                field.expect_wire_type(WIRE_LEN)?;
                cursor = Some(field.read_string()?);
            }
            _ => field.skip()?,
        }
    }

    Ok(PageRequest { page_size, cursor })
}

pub fn encode_message_page_response(response: &MessagePageResponse) -> SdkResult<Vec<u8>> {
    let mut output = Vec::new();
    for item in &response.items {
        let encoded = encode_message_item(item);
        encode_bytes_field(1, &encoded, &mut output);
    }
    let page_info = encode_page_info(response);
    encode_bytes_field(2, &page_info, &mut output);
    Ok(output)
}

pub fn decode_message_page_response(bytes: &[u8]) -> SdkResult<MessagePageResponse> {
    let mut items = Vec::new();
    let mut page_info = PageInfoWire::default();
    let mut decoder = ProtoDecoder::new(bytes, ProtobufTarget::MessagePageResponse);

    while let Some(field) = decoder.next_field()? {
        match field.number {
            1 => {
                field.expect_wire_type(WIRE_LEN)?;
                items.push(decode_message_item(&field.read_bytes()?)?);
            }
            2 => {
                field.expect_wire_type(WIRE_LEN)?;
                page_info = decode_page_info(&field.read_bytes()?)?;
            }
            _ => field.skip()?,
        }
    }

    Ok(Page {
        items,
        next_cursor: page_info.next_cursor,
        has_more: page_info.has_more,
    })
}

pub fn encode_mail_page_response(response: &MailPageResponse) -> SdkResult<Vec<u8>> {
    let mut output = Vec::new();
    for item in &response.items {
        let encoded = encode_mail_item(item);
        encode_bytes_field(1, &encoded, &mut output);
    }
    let page_info = encode_page_info(response);
    encode_bytes_field(2, &page_info, &mut output);
    Ok(output)
}

pub fn decode_mail_page_response(bytes: &[u8]) -> SdkResult<MailPageResponse> {
    let mut items = Vec::new();
    let mut page_info = PageInfoWire::default();
    let mut decoder = ProtoDecoder::new(bytes, ProtobufTarget::MailPageResponse);

    while let Some(field) = decoder.next_field()? {
        match field.number {
            1 => {
                field.expect_wire_type(WIRE_LEN)?;
                items.push(decode_mail_item(&field.read_bytes()?)?);
            }
            2 => {
                field.expect_wire_type(WIRE_LEN)?;
                page_info = decode_page_info(&field.read_bytes()?)?;
            }
            _ => field.skip()?,
        }
    }

    Ok(Page {
        items,
        next_cursor: page_info.next_cursor,
        has_more: page_info.has_more,
    })
}

const WIRE_VARINT: u8 = 0;
const WIRE_LEN: u8 = 2;

#[derive(Default)]
struct PageInfoWire {
    next_cursor: Option<String>,
    has_more: bool,
}

struct ProtoDecoder<'a> {
    bytes: &'a [u8],
    offset: usize,
    target: ProtobufTarget,
}

struct ProtoField<'a> {
    number: u32,
    wire_type: u8,
    bytes: &'a [u8],
    target: ProtobufTarget,
}

impl<'a> ProtoDecoder<'a> {
    fn new(bytes: &'a [u8], target: ProtobufTarget) -> Self {
        Self {
            bytes,
            offset: 0,
            target,
        }
    }

    fn next_field(&mut self) -> SdkResult<Option<ProtoField<'a>>> {
        if self.offset == self.bytes.len() {
            return Ok(None);
        }

        let key = read_varint(self.bytes, &mut self.offset, self.target.clone())?;
        let number = (key >> 3) as u32;
        let wire_type = (key & 0b111) as u8;

        if number == 0 {
            return Err(decode_error(
                self.target.clone(),
                ProtobufFailure::InvalidFieldNumber,
            ));
        }

        let start = self.offset;
        match wire_type {
            WIRE_VARINT => {
                read_varint(self.bytes, &mut self.offset, self.target.clone())?;
            }
            WIRE_LEN => {
                let len = read_varint(self.bytes, &mut self.offset, self.target.clone())? as usize;
                let end = self.offset.checked_add(len).ok_or_else(|| {
                    decode_error(self.target.clone(), ProtobufFailure::InvalidLength)
                })?;
                if end > self.bytes.len() {
                    return Err(decode_error(
                        self.target.clone(),
                        ProtobufFailure::InvalidLength,
                    ));
                }
                self.offset = end;
            }
            _ => {
                return Err(decode_error(
                    self.target.clone(),
                    ProtobufFailure::InvalidWireType {
                        field: number,
                        expected: WIRE_VARINT,
                        actual: wire_type,
                    },
                ));
            }
        }

        Ok(Some(ProtoField {
            number,
            wire_type,
            bytes: &self.bytes[start..self.offset],
            target: self.target.clone(),
        }))
    }
}

impl<'a> ProtoField<'a> {
    fn expect_wire_type(&self, expected: u8) -> SdkResult<()> {
        if self.wire_type == expected {
            Ok(())
        } else {
            Err(decode_error(
                self.target.clone(),
                ProtobufFailure::InvalidWireType {
                    field: self.number,
                    expected,
                    actual: self.wire_type,
                },
            ))
        }
    }

    fn read_u64(&self) -> SdkResult<u64> {
        let mut offset = 0;
        let value = read_varint(self.bytes, &mut offset, self.target.clone())?;
        if offset == self.bytes.len() {
            Ok(value)
        } else {
            Err(decode_error(
                self.target.clone(),
                ProtobufFailure::InvalidVarint,
            ))
        }
    }

    fn read_i32(&self) -> SdkResult<i32> {
        let value = self.read_u64()?;
        i32::try_from(value).map_err(|_| {
            decode_error(
                self.target.clone(),
                ProtobufFailure::NumericOverflow { field: self.number },
            )
        })
    }

    fn read_i64(&self) -> SdkResult<i64> {
        let value = self.read_u64()?;
        i64::try_from(value).map_err(|_| {
            decode_error(
                self.target.clone(),
                ProtobufFailure::NumericOverflow { field: self.number },
            )
        })
    }

    fn read_bool(&self) -> SdkResult<bool> {
        Ok(self.read_u64()? != 0)
    }

    fn read_bytes(&self) -> SdkResult<Vec<u8>> {
        let mut offset = 0;
        let len = read_varint(self.bytes, &mut offset, self.target.clone())? as usize;
        let end = offset
            .checked_add(len)
            .ok_or_else(|| decode_error(self.target.clone(), ProtobufFailure::InvalidLength))?;
        if end != self.bytes.len() {
            return Err(decode_error(
                self.target.clone(),
                ProtobufFailure::InvalidLength,
            ));
        }
        Ok(self.bytes[offset..end].to_vec())
    }

    fn read_string(&self) -> SdkResult<String> {
        String::from_utf8(self.read_bytes()?)
            .map_err(|_| decode_error(self.target.clone(), ProtobufFailure::InvalidUtf8))
    }

    fn skip(&self) -> SdkResult<()> {
        Ok(())
    }
}

fn encode_message_item(item: &MessageItem) -> Vec<u8> {
    let mut output = Vec::new();
    encode_string_field(1, &item.id, &mut output);
    encode_string_field(2, &item.conversation_name, &mut output);
    encode_int32_field(
        3,
        conversation_type_to_proto(&item.conversation_type),
        &mut output,
    );
    if let Some(avatar_url) = item.avatar_url.as_deref() {
        encode_string_field(4, avatar_url, &mut output);
    }
    encode_string_field(5, &item.avatar_text, &mut output);
    encode_string_field(6, &item.last_message_preview, &mut output);
    encode_int64_field(7, item.last_message_time_millis, &mut output);
    encode_int32_field(8, item.unread_count, &mut output);
    encode_bool_field(9, item.is_pinned, &mut output);
    encode_bool_field(10, item.is_muted, &mut output);
    encode_bool_field(11, item.is_bot, &mut output);
    output
}

fn decode_message_item(bytes: &[u8]) -> SdkResult<MessageItem> {
    let mut item = MessageItem {
        id: String::new(),
        conversation_name: String::new(),
        conversation_type: ConversationType::Single,
        avatar_url: None,
        avatar_text: String::new(),
        last_message_preview: String::new(),
        last_message_time_millis: 0,
        unread_count: 0,
        is_pinned: false,
        is_muted: false,
        is_bot: false,
    };
    let mut decoder = ProtoDecoder::new(bytes, ProtobufTarget::MessageItem);

    while let Some(field) = decoder.next_field()? {
        match field.number {
            1 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.id = field.read_string()?;
            }
            2 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.conversation_name = field.read_string()?;
            }
            3 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.conversation_type = conversation_type_from_proto(field.read_i32()?)?;
            }
            4 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.avatar_url = Some(field.read_string()?);
            }
            5 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.avatar_text = field.read_string()?;
            }
            6 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.last_message_preview = field.read_string()?;
            }
            7 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.last_message_time_millis = field.read_i64()?;
            }
            8 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.unread_count = field.read_i32()?;
            }
            9 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.is_pinned = field.read_bool()?;
            }
            10 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.is_muted = field.read_bool()?;
            }
            11 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.is_bot = field.read_bool()?;
            }
            _ => field.skip()?,
        }
    }

    Ok(item)
}

fn encode_mail_item(item: &MailItem) -> Vec<u8> {
    let mut output = Vec::new();
    encode_string_field(1, &item.id, &mut output);
    encode_string_field(2, &item.sender, &mut output);
    encode_string_field(3, &item.subject, &mut output);
    encode_string_field(4, &item.preview, &mut output);
    encode_int64_field(5, item.timestamp_millis, &mut output);
    encode_bool_field(6, item.unread, &mut output);
    encode_int32_field(7, item.attachment_count, &mut output);
    encode_int32_field(8, mail_type_to_proto(&item.mail_type), &mut output);
    if let Some(action_text) = item.action_text.as_deref() {
        encode_string_field(9, action_text, &mut output);
    }
    output
}

fn decode_mail_item(bytes: &[u8]) -> SdkResult<MailItem> {
    let mut item = MailItem {
        id: String::new(),
        sender: String::new(),
        subject: String::new(),
        preview: String::new(),
        timestamp_millis: 0,
        unread: false,
        attachment_count: 0,
        mail_type: MailType::Update,
        action_text: None,
    };
    let mut decoder = ProtoDecoder::new(bytes, ProtobufTarget::MailItem);

    while let Some(field) = decoder.next_field()? {
        match field.number {
            1 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.id = field.read_string()?;
            }
            2 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.sender = field.read_string()?;
            }
            3 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.subject = field.read_string()?;
            }
            4 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.preview = field.read_string()?;
            }
            5 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.timestamp_millis = field.read_i64()?;
            }
            6 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.unread = field.read_bool()?;
            }
            7 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.attachment_count = field.read_i32()?;
            }
            8 => {
                field.expect_wire_type(WIRE_VARINT)?;
                item.mail_type = mail_type_from_proto(field.read_i32()?)?;
            }
            9 => {
                field.expect_wire_type(WIRE_LEN)?;
                item.action_text = Some(field.read_string()?);
            }
            _ => field.skip()?,
        }
    }

    Ok(item)
}

fn encode_page_info<T>(page: &Page<T>) -> Vec<u8> {
    let mut output = Vec::new();
    if let Some(next_cursor) = page.next_cursor.as_deref() {
        encode_string_field(1, next_cursor, &mut output);
    }
    encode_bool_field(2, page.has_more, &mut output);
    output
}

fn decode_page_info(bytes: &[u8]) -> SdkResult<PageInfoWire> {
    let mut page_info = PageInfoWire::default();
    let mut decoder = ProtoDecoder::new(bytes, ProtobufTarget::PageInfo);

    while let Some(field) = decoder.next_field()? {
        match field.number {
            1 => {
                field.expect_wire_type(WIRE_LEN)?;
                let cursor = field.read_string()?;
                if !cursor.is_empty() {
                    page_info.next_cursor = Some(cursor);
                }
            }
            2 => {
                field.expect_wire_type(WIRE_VARINT)?;
                page_info.has_more = field.read_bool()?;
            }
            _ => field.skip()?,
        }
    }

    Ok(page_info)
}

fn conversation_type_to_proto(value: &ConversationType) -> i32 {
    match value {
        ConversationType::Single => 1,
        ConversationType::Group => 2,
        ConversationType::Bot => 3,
    }
}

fn conversation_type_from_proto(value: i32) -> SdkResult<ConversationType> {
    match value {
        0 | 1 => Ok(ConversationType::Single),
        2 => Ok(ConversationType::Group),
        3 => Ok(ConversationType::Bot),
        _ => Err(decode_error(
            ProtobufTarget::MessageItem,
            ProtobufFailure::InvalidEnum { field: 3, value },
        )),
    }
}

fn mail_type_to_proto(value: &MailType) -> i32 {
    match value {
        MailType::Reminder => 1,
        MailType::System => 2,
        MailType::Collaboration => 3,
        MailType::Report => 4,
        MailType::Update => 5,
    }
}

fn mail_type_from_proto(value: i32) -> SdkResult<MailType> {
    match value {
        1 => Ok(MailType::Reminder),
        2 => Ok(MailType::System),
        3 => Ok(MailType::Collaboration),
        4 => Ok(MailType::Report),
        0 | 5 => Ok(MailType::Update),
        _ => Err(decode_error(
            ProtobufTarget::MailItem,
            ProtobufFailure::InvalidEnum { field: 8, value },
        )),
    }
}

fn encode_string_field(number: u32, value: &str, output: &mut Vec<u8>) {
    encode_bytes_field(number, value.as_bytes(), output);
}

fn encode_bytes_field(number: u32, value: &[u8], output: &mut Vec<u8>) {
    encode_key(number, WIRE_LEN, output);
    encode_varint(value.len() as u64, output);
    output.extend_from_slice(value);
}

fn encode_int32_field(number: u32, value: i32, output: &mut Vec<u8>) {
    encode_key(number, WIRE_VARINT, output);
    encode_varint(value as u64, output);
}

fn encode_int64_field(number: u32, value: i64, output: &mut Vec<u8>) {
    encode_key(number, WIRE_VARINT, output);
    encode_varint(value as u64, output);
}

fn encode_bool_field(number: u32, value: bool, output: &mut Vec<u8>) {
    encode_key(number, WIRE_VARINT, output);
    encode_varint(u64::from(value), output);
}

fn encode_key(number: u32, wire_type: u8, output: &mut Vec<u8>) {
    encode_varint(((number as u64) << 3) | u64::from(wire_type), output);
}

fn encode_varint(mut value: u64, output: &mut Vec<u8>) {
    while value >= 0x80 {
        output.push((value as u8) | 0x80);
        value >>= 7;
    }
    output.push(value as u8);
}

fn read_varint(bytes: &[u8], offset: &mut usize, target: ProtobufTarget) -> SdkResult<u64> {
    let mut result = 0u64;

    for shift in (0..64).step_by(7) {
        let byte = *bytes
            .get(*offset)
            .ok_or_else(|| decode_error(target.clone(), ProtobufFailure::InvalidVarint))?;
        *offset += 1;
        result |= u64::from(byte & 0x7f) << shift;

        if byte & 0x80 == 0 {
            return Ok(result);
        }
    }

    Err(decode_error(target, ProtobufFailure::InvalidVarint))
}

fn usize_to_i32(value: usize, target: ProtobufTarget, field: u32) -> SdkResult<i32> {
    i32::try_from(value)
        .map_err(|_| encode_error(target, ProtobufFailure::NumericOverflow { field }))
}

fn i32_to_usize(value: i32, target: ProtobufTarget, field: u32) -> SdkResult<usize> {
    usize::try_from(value)
        .map_err(|_| decode_error(target, ProtobufFailure::NumericOverflow { field }))
}

fn decode_error(target: ProtobufTarget, failure: ProtobufFailure) -> SdkError {
    SdkError::ProtobufDecode { target, failure }
}

fn encode_error(target: ProtobufTarget, failure: ProtobufFailure) -> SdkError {
    SdkError::ProtobufEncode { target, failure }
}

fn create_message(index: usize) -> MessageItem {
    let conversation_type = conversation_type_for(index);
    let conversation_name = conversation_name_for(index, &conversation_type);

    MessageItem {
        id: format!("message-{}", index + 1),
        avatar_text: avatar_text_for(&conversation_name),
        avatar_url: None,
        last_message_preview: last_message_preview_for(index, &conversation_type),
        last_message_time_millis: DEFAULT_BASE_TIME_MILLIS - index as i64 * MESSAGE_INTERVAL_MILLIS,
        unread_count: unread_count_for(index),
        is_pinned: index % 17 == 0,
        is_muted: index % 11 == 0,
        is_bot: conversation_type == ConversationType::Bot,
        conversation_name,
        conversation_type,
    }
}

fn conversation_type_for(index: usize) -> ConversationType {
    if index % 9 == 0 {
        ConversationType::Bot
    } else if index % 3 == 0 {
        ConversationType::Group
    } else {
        ConversationType::Single
    }
}

fn conversation_name_for(index: usize, conversation_type: &ConversationType) -> String {
    let names = match conversation_type {
        ConversationType::Single => &SINGLE_NAMES[..],
        ConversationType::Group => &GROUP_NAMES[..],
        ConversationType::Bot => &BOT_NAMES[..],
    };

    format!("{} {}", names[index % names.len()], index + 1)
}

fn avatar_text_for(name: &str) -> String {
    name.chars()
        .next()
        .map(|character| character.to_ascii_uppercase().to_string())
        .unwrap_or_else(|| "M".to_owned())
}

fn last_message_preview_for(index: usize, conversation_type: &ConversationType) -> String {
    let previews = match conversation_type {
        ConversationType::Single => &SINGLE_PREVIEWS[..],
        ConversationType::Group => &GROUP_PREVIEWS[..],
        ConversationType::Bot => &BOT_PREVIEWS[..],
    };

    previews[index % previews.len()].to_owned()
}

fn unread_count_for(index: usize) -> i32 {
    if index % 13 == 0 {
        99
    } else if index % 4 == 0 {
        (index % 12 + 1) as i32
    } else {
        0
    }
}

fn create_mail(index: usize) -> MailItem {
    MailItem {
        id: format!("mail-{}", index + 1),
        sender: MAIL_SENDERS[index % MAIL_SENDERS.len()].to_owned(),
        subject: format!(
            "{} #{}",
            MAIL_SUBJECTS[index % MAIL_SUBJECTS.len()],
            index + 1
        ),
        preview: MAIL_PREVIEWS[index % MAIL_PREVIEWS.len()].to_owned(),
        timestamp_millis: DEFAULT_BASE_TIME_MILLIS - index as i64 * MAIL_INTERVAL_MILLIS,
        unread: index % 3 == 0,
        attachment_count: (index % 4) as i32,
        mail_type: mail_type_for(index),
        action_text: (index % 5 == 0).then(|| "View".to_owned()),
    }
}

fn mail_type_for(index: usize) -> MailType {
    match index % 5 {
        0 => MailType::Reminder,
        1 => MailType::System,
        2 => MailType::Collaboration,
        3 => MailType::Report,
        _ => MailType::Update,
    }
}

const SINGLE_NAMES: [&str; 6] = [
    "Alex Chen",
    "Mia Zhang",
    "Noah Liu",
    "Emma Wang",
    "Kai Huang",
    "Nina Zhao",
];

const GROUP_NAMES: [&str; 5] = [
    "Product Squad",
    "Android Guild",
    "Release Room",
    "Design Review",
    "Training Camp",
];

const BOT_NAMES: [&str; 4] = ["Calendar Bot", "Approval Bot", "Build Bot", "Docs Bot"];

const SINGLE_PREVIEWS: [&str; 4] = [
    "Can you review the latest draft?",
    "I pushed the small fix we discussed.",
    "Let's sync after the standup.",
    "The new mock data looks good to me.",
];

const GROUP_PREVIEWS: [&str; 4] = [
    "Meeting notes are ready for review.",
    "Please update your progress before 6 PM.",
    "The first page flow is ready for smoke testing.",
    "We still need mapper coverage for the shared model.",
];

const BOT_PREVIEWS: [&str; 4] = [
    "You have a pending approval request.",
    "Daily build completed successfully.",
    "A calendar event starts in 10 minutes.",
    "Documentation reminder: add acceptance evidence.",
];

const MAIL_SENDERS: [&str; 6] = [
    "Feishu Updates",
    "Product Ops",
    "Design Team",
    "QA Desk",
    "Build System",
    "Learning Center",
];

const MAIL_SUBJECTS: [&str; 6] = [
    "Weekly product digest",
    "Action required for release",
    "Design review notes",
    "Regression test summary",
    "Build pipeline report",
    "Training reminder",
];

const MAIL_PREVIEWS: [&str; 6] = [
    "Here are the highlights and decisions from this week.",
    "Please confirm owners before the release window closes.",
    "The annotated mockups are ready for implementation review.",
    "Smoke testing passed with a few follow-up checks remaining.",
    "Nightly build finished and artifacts are available.",
    "Your assigned course is due later this week.",
];

#[cfg(test)]
mod tests {
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
}
