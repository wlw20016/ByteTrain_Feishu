mod async_api;
mod error;
mod mock_data;
mod models;
mod paging;
mod protobuf;

pub use async_api::{
    get_mail_page, get_message_page, read_mail_page, read_message_page, MockFeedSdk,
};
pub use error::{ProtobufFailure, ProtobufTarget, SdkError, SdkResult};
pub use mock_data::{generate_mail_items, generate_message_items};
pub use models::{
    ConversationType, MailItem, MailPageResponse, MailType, MessageItem, MessagePageResponse, Page,
    PageRequest,
};
pub use paging::{DEFAULT_TOTAL_COUNT, MAX_PAGE_SIZE, MIN_PAGE_SIZE};
pub use protobuf::{
    decode_mail_page_response, decode_message_page_response, decode_page_request,
    encode_mail_page_response, encode_message_page_response, encode_page_request,
};

#[cfg(test)]
mod tests;
