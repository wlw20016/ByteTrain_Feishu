use crate::error::SdkResult;
use crate::mock_data::{generate_mail_items, generate_message_items};
use crate::models::{
    MailItem, MailPageResponse, MessageItem, MessagePageResponse, Page, PageRequest,
};
use crate::paging::{paginate, DEFAULT_TOTAL_COUNT};

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
