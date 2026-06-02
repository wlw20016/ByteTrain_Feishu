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
