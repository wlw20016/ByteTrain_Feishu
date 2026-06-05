## Why

The project currently has Rust SDK logic, protobuf contracts, and Kotlin SDK-backed repository adapters, but the Android UI still runs on Kotlin mock repositories. The course requires the SDK/protobuf layer to be more than a documented boundary, so this change closes the runtime path from UI-facing repositories to SDK/protobuf-backed data.

## What Changes

- Add a runtime SDK integration path behind `MessageRepository` and `MailRepository`.
- Serialize or map SDK page requests/responses through the protobuf contract shape.
- Keep Kotlin mock repositories as a fallback while the runtime bridge is verified.
- Add focused verification for message and mail first page, pagination, error fallback, and field mapping.
- Record build/test evidence and any remaining runtime limitations.

## Capabilities

### New Capabilities

- `sdk-runtime-integration`: Runtime integration between Android UI repositories, SDK client boundary, Rust SDK data behavior, and protobuf-shaped contracts.

### Modified Capabilities

- None.

## Impact

- `features/message/data/`
- `features/mail/data/`
- `proto/`
- `sdk/rust/`
- `app/src/main/kotlin/`
- `docs/ai-context/`
- Bazel and Rust verification commands
