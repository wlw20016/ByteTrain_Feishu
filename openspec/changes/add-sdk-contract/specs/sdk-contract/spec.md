# SDK Contract Delta

## ADDED Requirements

### Requirement: SDK and UI SHALL share explicit data contracts

Message, mail, and paging contracts SHALL be represented in protobuf and aligned with Kotlin domain models.

#### Scenario: A message page is requested

- Given the UI requests a message page with page size and cursor
- When the SDK contract is used
- Then the response contains message items, next cursor, and has-more information with the agreed semantics

### Requirement: Rust SDK SHALL provide mock paged data

The Rust SDK SHALL provide deterministic mock message and mail page APIs for development and validation.

#### Scenario: Last page is requested

- Given the cursor points to the final page
- When the SDK returns data
- Then `has_more` is false and no invalid next page is advertised

### Requirement: UI SHALL depend on repository interfaces, not SDK internals

Android UI code SHALL use repository interfaces and adapters so the data source can switch from Kotlin mock to SDK-backed implementation.

#### Scenario: Data source changes

- Given the app switches from mock repository to SDK adapter
- When the list screen loads data
- Then UI rendering and paging behavior remain unchanged

