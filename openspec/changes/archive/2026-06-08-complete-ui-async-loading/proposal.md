# Change: Complete UI Async Loading

## Why

The message and mail repositories expose `suspend` APIs, and the SDK clients are shaped as asynchronous boundaries. However, `MainActivity` currently calls them through `runSuspendBlocking`, which blocks the caller while the page request completes. This does not fully satisfy the requirement that UI data loading switch from synchronous reads to asynchronous reads.

## What Changes

- Replace blocking page loads in `MainActivity` with lifecycle-aware asynchronous loading.
- Keep message and mail initial-page and next-page flows responsive while requests are in progress.
- Render explicit loading, loading-more, error, and load-more-error states from `PagingUiState`.
- Preserve existing list position behavior after async next-page completion.
- Keep SDK/native fallback behavior unchanged.

## Impact

- Affects Android app orchestration and message/mail list rendering states.
- Does not change proto, Rust SDK, or repository contracts.
- Requires focused checks proving no blocking helper remains in UI load paths.
