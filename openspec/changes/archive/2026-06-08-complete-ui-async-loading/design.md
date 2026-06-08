# Design: Complete UI Async Loading

## Current State

`MessageRepository.loadPage` and `MailRepository.loadPage` are `suspend` functions, but `MainActivity` wraps calls with `runSuspendBlocking`. This keeps the public repository contract asynchronous while the actual UI flow still waits synchronously.

## Approach

- Introduce a small UI load coordinator inside `MainActivity` or an app-level controller that launches repository work off the main call path.
- Use Android platform primitives already available in the project. Prefer a minimal `CoroutineScope` if coroutine dependencies are already present; otherwise use an executor plus main-thread posting.
- Track first-page and next-page state independently for message and mail.
- Render:
  - first-page loading before content exists,
  - content when data exists,
  - first-page error when no content exists,
  - loading-more while preserving loaded content,
  - load-more error while preserving loaded content.
- Guard duplicate load-more triggers while a request is in flight.
- Restore scroll before draw after async append, keeping the current no-jump behavior.

## Validation

- Add a focused check that fails if `MainActivity` still contains `runSuspendBlocking`.
- Add tests or checks that message and mail load paths update `PagingUiState` instead of blocking.
- Run `.\gradlew.bat test` and `.\gradlew.bat :app:assembleDebug`.
- Run relevant Bazel app build after Kotlin changes.
