# Mapper field test matrix

This document records the TEST-002 mapper coverage for the first UI main-flow phase.

The automated acceptance script is:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-test-002.ps1
```

## Scope

The matrix covers the two domain-to-UI mappers used by the Android UI:

- Message mapper: `features/message/mapper/MessageUiMapper.kt`
- Mail mapper: `features/mail/mapper/MailUiMapper.kt`

Both mappers convert feature domain models to `UnifiedListItem`, which is the shared list/detail rendering model.

## Message mapper

| Unified field | Source field or rule |
| --- | --- |
| `id` | `MessageItem.id` |
| `title` | `MessageItem.conversationName` |
| `subtitle` | `MessageItem.lastMessagePreview` |
| `timestampText` | formatted `MessageItem.lastMessageTimeMillis` |
| `avatar` | `avatarText`, `avatarUrl`, and conversation-type background color |
| `badges` | unread count, pinned state, muted state, and bot state |
| `displayStyle` | `DisplayStyle.DENSE_CONVERSATION` |
| `detail` | conversation title, preview body, type, unread count, pinned, muted, bot, and time metas |

## Mail mapper

| Unified field | Source field or rule |
| --- | --- |
| `id` | `MailItem.id` |
| `title` | `MailItem.subject` |
| `subtitle` | `MailItem.preview` |
| `timestampText` | formatted `MailItem.timestampMillis` |
| `avatar` | sender first letter and mail-type background color |
| `badges` | unread state, attachment count, mail type, and optional action text |
| `displayStyle` | `DisplayStyle.MAIL_CARD` |
| `detail` | subject title, preview body, sender, type, unread, attachment count, optional action, and time metas |

## Evidence

`scripts/check-test-002.ps1` verifies that both mapper implementations preserve the key fields required by shared list and detail rendering:

- stable ids
- list title and subtitle text
- formatted timestamp text
- avatar data
- badges for state and business metadata
- display style selection
- detail title, body, and meta rows

This keeps message and mail rendering aligned with the shared UI model before broader Kotlin/JUnit test infrastructure is introduced.
