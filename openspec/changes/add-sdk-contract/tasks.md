# Tasks: add-sdk-contract

## 1. Proto Contract

- [ ] SDK-001 Expand `message.proto`, `mail.proto`, and `paging.proto` to match the agreed Kotlin domain fields.
- [ ] SDK-002 Define message and mail page response messages or an equivalent shared paging response strategy.
- [ ] Document cursor semantics and compatibility expectations in `design.md` or proto comments.

## 2. Rust SDK

- [ ] SDK-003 Implement Rust mock data models and deterministic 10000-record generators for messages and mails.
- [ ] SDK-004 Implement Rust pagination APIs for message and mail data.
- [ ] Add Rust unit tests for first page, middle page, final page, invalid cursor, and page size boundaries.

## 3. Async and Adapter Boundary

- [ ] SDK-005 Define the Kotlin-to-SDK async boundary and error mapping strategy.
- [ ] SDK-006 Implement SDK-backed repository adapters after the UI mock path is stable.
- [ ] Keep Kotlin mock repositories available as fallback until SDK integration is validated.

## 4. Evidence

- [ ] Record AI-generated protocol suggestions and human decisions.
- [ ] Record `cargo test` or Bazel test results when available.
- [ ] Update Feishu Bitable rows for SDK/proto tasks with OpenSpec evidence links.
