use crate::error::SdkResult;
use crate::error::{ProtobufFailure, ProtobufTarget, SdkError};
use crate::models::{
    ConversationType, MailItem, MailPageResponse, MailType, MessageItem, MessagePageResponse, Page,
    PageRequest,
};

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
