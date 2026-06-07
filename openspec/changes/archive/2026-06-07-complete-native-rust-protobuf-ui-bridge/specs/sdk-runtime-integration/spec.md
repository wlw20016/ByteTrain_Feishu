## MODIFIED Requirements

### Requirement: Runtime SDK responses must align with protobuf page contracts

The SDK runtime bridge MUST use actual protobuf request and response bytes equivalent to `PageRequest`, `PageInfo`, `MessagePageResponse`, and `MailPageResponse`. A Kotlin-only runtime implementation that merely mirrors protobuf-shaped fields is allowed only as fallback or test fake, not as proof of production SDK protobuf runtime integration.

#### Scenario: First page request

- **WHEN** the UI-facing repository requests a page with an empty cursor on the production SDK-backed path
- **THEN** the SDK runtime bridge sends `PageRequest` protobuf bytes to Rust SDK and returns the first page plus page metadata decoded from Rust SDK protobuf response bytes

#### Scenario: Next page request

- **WHEN** the UI-facing repository passes back a non-empty `nextCursor` on the production SDK-backed path
- **THEN** the SDK runtime bridge passes that cursor through `PageRequest` protobuf bytes to Rust SDK and returns the next page according to Rust SDK pagination semantics

#### Scenario: Kotlin runtime fake is not production bridge evidence

- **WHEN** a test or runtime path uses `RuntimeMessageSdkClient` or `RuntimeMailSdkClient`
- **THEN** it MUST be treated as fallback/fake behavior and MUST NOT be documented as real Rust SDK protobuf communication
