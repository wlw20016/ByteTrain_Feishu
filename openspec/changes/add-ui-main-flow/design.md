# Design: add-ui-main-flow

## Scope

This change delivers the first visible app milestone. Rust SDK and full Bazel wiring are not blocking this change; they are handled by `add-sdk-contract` and `wire-bazel-build`.

## Architecture

```text
app
  -> shared/navigation
  -> features/message
  -> features/mail
features/message, features/mail
  -> shared/list
  -> shared/ui models
```

## UI Model

Add a unified UI model so message and mail can share list/detail rendering:

- `UnifiedListItem`
- `AvatarModel`
- `BadgeModel`
- `DisplayStyle`
- `DetailModel`
- `DetailMeta`

Message and mail keep separate business models, then map into the unified UI model:

- `MessageItem -> UnifiedListItem`
- `MailItem -> UnifiedListItem`

## Paging State

Extend the existing `PagingUiState<T>` to cover:

- Loading
- Empty
- Error
- Content
- LoadingMore
- LoadMoreError

Pagination must not render all 10000 records at once. Repositories expose `loadPage(pageSize, cursor)` and return `items`, `nextCursor`, and `hasMore`.

## Screens

- `MainActivity`: app entry and root composition.
- Message tab: high-density Feishu-style conversation list.
- Message detail: title, preview/body, time, unread/pinned/muted/bot metadata.
- Mail tab: QQ-mail-reminder-style card list.
- Mail detail: subject, sender, preview/body, received time, attachment/type metadata.

## Acceptance Strategy

- Repository tests cover pagination boundaries.
- Mapper tests cover key field mapping.
- Manual validation covers tab switching, first page loading, load-more, detail navigation, and return behavior.
- OpenSpec `tasks.md` records screenshots or manual validation notes if UI test infrastructure is not ready.
