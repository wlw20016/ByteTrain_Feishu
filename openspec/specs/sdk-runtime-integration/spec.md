# sdk-runtime-integration Specification

## Purpose
TBD - created by archiving change connect-sdk-protobuf-runtime. Update Purpose after archive.
## Requirements
### Requirement: SDK-backed repositories must be constructible at app runtime

The system MUST provide an app-level construction path for `MessageRepository` and `MailRepository` that can use SDK-backed repositories while preserving Kotlin mock repositories as fallback.

#### Scenario: Message repository uses SDK-backed path

- **WHEN** the app composes the message data source
- **THEN** it can construct a `MessageRepository` through the SDK-backed repository path without importing SDK DTOs into UI screens

#### Scenario: Mail repository uses SDK-backed path

- **WHEN** the app composes the mail data source
- **THEN** it can construct a `MailRepository` through the SDK-backed repository path without importing SDK DTOs into UI screens

### Requirement: Runtime SDK responses must align with protobuf page contracts

The SDK runtime bridge MUST use request and response semantics equivalent to `PageRequest`, `PageInfo`, `MessagePageResponse`, and `MailPageResponse`.

#### Scenario: First page request

- **WHEN** the UI-facing repository requests a page with an empty cursor
- **THEN** the SDK runtime bridge returns the first page and page metadata equivalent to the protobuf page contract

#### Scenario: Next page request

- **WHEN** the UI-facing repository passes back a non-empty `nextCursor`
- **THEN** the SDK runtime bridge treats the cursor as opaque and returns the next page according to SDK pagination semantics

### Requirement: SDK runtime failures must preserve UI availability

The SDK-backed repository path MUST map runtime errors to a documented error/fallback strategy and MUST keep the existing mock repository fallback available until native SDK integration is fully verified.

#### Scenario: SDK runtime failure

- **WHEN** the SDK runtime client fails while loading a message or mail page
- **THEN** the repository either returns a documented domain error or delegates to the configured mock fallback without crashing the UI

