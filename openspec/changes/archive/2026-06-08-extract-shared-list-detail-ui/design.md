# Design: Extract Shared List Detail UI

## Current State

`UnifiedListItem`, `AvatarModel`, `BadgeModel`, and `DetailModel` are shared. `MessageListScreen`, `MailListScreen`, `MessageDetailScreen`, and `MailDetailScreen` still duplicate Android View construction logic and pagination behavior.

## Approach

- Introduce shared UI builders in `shared/ui` or a new `shared/list-ui` target.
- Model the reusable list shell around:
  - title and total label,
  - list items,
  - `hasMore`,
  - `isLoadingMore`,
  - initial scroll position,
  - item renderer,
  - load-more callback,
  - open-detail callback.
- Keep message row and mail card differences behind renderer callbacks or small style configs.
- Extract badge row, footer state, color parsing, dp conversion, and pre-draw scroll restoration.
- Extract shared detail header behavior and reusable detail content primitives where the message chat and mail reading layouts overlap.
- Keep message chat bubbles and mail body content feature-specific because they are distinct product surfaces.

## Validation

- Add checks that shared scroll restoration, footer, and badge helpers are used by both message and mail.
- Add Bazel build verification for shared and feature UI targets.
- Add regression checks that message and mail still expose list, load-more, and detail flows.
