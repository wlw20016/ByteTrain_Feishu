# Tasks: add-ui-main-flow

## 1. App Shell and Navigation

- [ ] UI-001 Implement a real Android `MainActivity` instead of the current placeholder class.
- [ ] UI-002 Implement bottom tabs for Message and Mail, using `AppRoutes` consistently.

## 2. Shared UI and Paging

- [ ] UI-003 Add unified UI list/detail models: `UnifiedListItem`, `AvatarModel`, `BadgeModel`, `DisplayStyle`, `DetailModel`, `DetailMeta`.
- [ ] UI-004 Extend `PagingUiState` with load-more and load-more-error states.
- [ ] TEST-003 Document and validate the UI state matrix: Loading, Empty, Error, Content, LoadingMore, LoadMoreError.

## 3. Message Flow

- [ ] MSG-001 Expand `MessageItem` to include conversation name/type, avatar, last message preview/time, unread count, pinned, muted, and bot fields.
- [ ] MSG-002 Implement `MockMessageRepository` with 10000 records and cursor pagination.
- [ ] MSG-003 Implement `MessageItem -> UnifiedListItem` mapping.
- [ ] MSG-004 Implement the message list screen.
- [ ] MSG-005 Implement load-more behavior for the message list.
- [ ] MSG-006 Implement the message detail screen.

## 4. Mail Flow

- [ ] MAIL-001 Expand `MailItem` to include sender name, subject, preview, received time, unread, attachment, mail type, and action text.
- [ ] MAIL-002 Implement `MockMailRepository` with 10000 records and cursor pagination.
- [ ] MAIL-003 Implement `MailItem -> UnifiedListItem` mapping.
- [ ] MAIL-004 Implement the mail card list screen.
- [ ] MAIL-005 Implement load-more behavior for the mail list.
- [ ] MAIL-006 Implement the mail detail screen.

## 5. Tests and Evidence

- [ ] TEST-001 Add repository pagination tests for first page, next page, final page, invalid cursor, and empty result.
- [ ] TEST-002 Add mapper tests for message/mail mapping into unified UI models.
- [ ] TEST-004 Validate 10000-record scroll behavior and record manual evidence.
- [ ] REL-001 Complete first-stage UI acceptance and record evidence in this file.

## 6. Documentation

- [ ] DOC-001 Record AI prompts, AI conclusions, human decisions, and final results for this UI change.
- [ ] Update Bitable rows for all UI tasks with status, owner, and evidence links.
