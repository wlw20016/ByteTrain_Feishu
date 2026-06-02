# SDK adapter evidence

This document records the SDK-006 adapter boundary after the UI mock main flow stabilized.

Automated check:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-sdk-006.ps1
```

## Scope

The first SDK-backed repository adapter pass adds:

- `SdkMessageRepository`
- `SdkMailRepository`

Both adapters implement the existing UI-facing repository interfaces:

- `MessageRepository`
- `MailRepository`

The UI can therefore keep rendering through the same `loadPage(pageSize, cursor)` contract while the data source switches from Kotlin mock repositories to SDK-backed repositories.

## Boundary

The SDK adapter uses explicit SDK client boundaries:

- `MessageSdkClient.getMessagePage(pageSize, cursor)`
- `MailSdkClient.getMailPage(pageSize, cursor)`

Each client returns SDK DTO pages:

- `SdkMessagePage`
- `SdkMailPage`

Each page contains SDK DTO items, `nextCursor`, and `hasMore`. The adapter maps DTO items into Kotlin domain models before returning to the UI-facing repository interface.

## Fallback

Both adapters accept an optional fallback repository:

- `fallbackRepository: MessageRepository?`
- `fallbackRepository: MailRepository?`

If SDK-backed loading fails and a fallback exists, the adapter delegates to the Kotlin mock repository path. This preserves the existing mock repository fallback until native SDK integration and runtime verification are complete.

## Notes

This is an SDK-backed repository adapter boundary, not a native FFI binding. The real Rust/protobuf transport can be attached behind the SDK client interfaces without changing UI code.
