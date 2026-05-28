# Proposal: add-ui-main-flow

## Why

The current repository only contains an Android entry placeholder and domain model skeletons. The course requires a runnable Android app with message and mail tabs, 10000 mock records per tab, pagination, reusable list/detail abstractions, and documented UI state handling.

## What

- Implement a real Android app entry and two-tab navigation.
- Add a shared UI list model and complete paging state model.
- Implement message list, message detail, mail list, and mail detail flows.
- Add mock repositories with 10000 records and cursor/page-size based pagination.
- Reuse shared list, paging, and detail abstractions across message and mail.
- Add UI state matrix, mapper tests, repository tests, and manual validation evidence.

## Impact

- Affects `app/`, `features/message/`, `features/mail/`, `shared/list`, `shared/ui`, and `shared/navigation`.
- Requires follow-up Bazel/Gradle validation depending on the selected Android toolchain.
- Defines the first functional acceptance milestone for the project.
