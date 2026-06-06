use crate::models::{ConversationType, MailItem, MailType, MessageItem};

const DEFAULT_BASE_TIME_MILLIS: i64 = 1_717_200_000_000;
const MESSAGE_INTERVAL_MILLIS: i64 = 60_000;
const MAIL_INTERVAL_MILLIS: i64 = 300_000;

pub fn generate_message_items(total_count: usize) -> Vec<MessageItem> {
    (0..total_count).map(create_message).collect()
}

pub fn generate_mail_items(total_count: usize) -> Vec<MailItem> {
    (0..total_count).map(create_mail).collect()
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
