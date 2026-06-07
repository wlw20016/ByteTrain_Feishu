# native-rust-protobuf-ui-bridge Specification

## Purpose
TBD - created by archiving change complete-native-rust-protobuf-ui-bridge. Update Purpose after archive.
## Requirements
### Requirement: Android SDK client MUST communicate with Rust SDK through protobuf bytes

Android SDK client MUST encode repository page requests as protobuf bytes, call a Rust SDK bridge, and decode protobuf response bytes before returning SDK DTOs to repository adapters.

#### Scenario: Message page request uses Rust protobuf bridge

- **WHEN** `MessageSdkClient.getMessagePage(pageSize, cursor)` is called on the production SDK-backed path
- **THEN** the client encodes a `PageRequest` protobuf payload, invokes the Rust message page bridge, decodes a `MessagePageResponse` protobuf payload, and returns mapped SDK message DTOs

#### Scenario: Mail page request uses Rust protobuf bridge

- **WHEN** `MailSdkClient.getMailPage(pageSize, cursor)` is called on the production SDK-backed path
- **THEN** the client encodes a `PageRequest` protobuf payload, invokes the Rust mail page bridge, decodes a `MailPageResponse` protobuf payload, and returns mapped SDK mail DTOs

### Requirement: Rust SDK bridge MUST own protobuf request decoding and response encoding

Rust SDK bridge MUST accept `PageRequest` bytes, decode them using Rust SDK protobuf logic, read the requested page through Rust SDK pagination APIs, and return response bytes encoded by Rust SDK protobuf logic.

#### Scenario: Rust bridge returns message response bytes

- **WHEN** the Rust message bridge receives valid `PageRequest` bytes
- **THEN** it returns `MessagePageResponse` bytes containing items and page metadata from Rust SDK pagination

#### Scenario: Rust bridge returns mail response bytes

- **WHEN** the Rust mail bridge receives valid `PageRequest` bytes
- **THEN** it returns `MailPageResponse` bytes containing items and page metadata from Rust SDK pagination

### Requirement: Bridge failures MUST be structured and fallback-compatible

Bridge failures MUST preserve enough structured information for Kotlin clients to distinguish invalid request, protobuf failure, Rust SDK read failure, and native bridge failure.

#### Scenario: Invalid protobuf request

- **WHEN** Android passes malformed `PageRequest` bytes to the Rust bridge
- **THEN** the bridge returns a structured protobuf decode failure that the Kotlin client can map to repository fallback or caller-visible error

#### Scenario: Native bridge unavailable

- **WHEN** the production SDK client cannot load or call the native Rust bridge
- **THEN** the repository construction or repository load path delegates to the configured fallback without crashing UI screens

### Requirement: Verification MUST prove Rust bridge is used

Tests MUST prove that the SDK-backed repository path can load message and mail pages through the Rust protobuf bridge, not only through Kotlin runtime fake data.

#### Scenario: Fallback disabled bridge smoke test

- **WHEN** fallback is disabled and the production native protobuf SDK client requests the first message and mail pages
- **THEN** both pages load successfully from Rust SDK bridge and expose expected pagination metadata

#### Scenario: Kotlin runtime fake remains isolated

- **WHEN** tests intentionally use `RuntimeMessageSdkClient` or `RuntimeMailSdkClient`
- **THEN** those tests are marked as fake/runtime fallback tests and do not count as proof of native Rust protobuf bridge integration

