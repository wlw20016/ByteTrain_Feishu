use std::fmt;

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
