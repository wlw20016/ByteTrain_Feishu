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
