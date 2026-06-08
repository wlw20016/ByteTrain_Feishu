# Tasks

- [x] UI-ASYNC-001 Document the current blocking load path and chosen async execution strategy.
  - Evidence: Added `docs/ai-context/ui/ui-async-loading-strategy.md` covering the previous `MainActivity -> runSuspendBlocking -> repository.loadPage` path and the chosen executor + main-thread handler async strategy.
- [x] UI-ASYNC-002 Replace `runSuspendBlocking` first-page loads with asynchronous message and mail initial loading.
  - Evidence: `MainActivity` now starts message and mail first-page repository reads through `launchRepositoryLoad`, which uses a background `ExecutorService`, `startCoroutine`, and `Handler(Looper.getMainLooper())` callbacks instead of `runSuspendBlocking`.
- [x] UI-ASYNC-003 Replace blocking load-more calls with asynchronous append flows and duplicate-trigger guards.
  - Evidence: `loadNextMessagePage` and `loadNextMailPage` now return while `isLoadingMoreMessages` / `isLoadingMoreMails` is true, enter `PagingUiState.LoadingMore`, and append results only from async success callbacks.
- [x] UI-ASYNC-004 Wire `PagingUiState` into message and mail list rendering for loading, content, error, loading-more, and load-more-error states.
  - Evidence: `MainActivity` stores `messagePagingState` and `mailPagingState`, maps them to `PagingUiState<UnifiedListItem>`, and passes `state = messageListUiState()` / `state = mailListUiState()` into the list screens. `MessageListScreen` and `MailListScreen` render `Loading`, `Empty`, `Error`, `Content`, `LoadingMore`, and `LoadMoreError`.
- [x] UI-ASYNC-005 Preserve scroll restoration after asynchronous next-page completion.
  - Evidence: load-more callbacks still capture `scrollY` into `messageListScrollY` / `mailListScrollY`; both loading-more and async completion rerenders pass `initialScrollY`, and both list screens still restore with `ViewTreeObserver.OnPreDrawListener`.
- [x] UI-ASYNC-006 Add focused checks that reject blocking UI load helpers and verify async state wiring.
  - Evidence: Added `scripts/checks/ui/check-ui-async-loading.ps1`; `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\ui\check-ui-async-loading.ps1` output: `UI async loading check passed.`
- [x] UI-ASYNC-007 Run Gradle unit/build verification and Bazel app build; record outputs in this tasks file.
  - Evidence: `.\gradlew.bat test` output: `BUILD SUCCESSFUL in 1m 26s` with `49 actionable tasks: 10 executed, 39 up-to-date`. Kotlin daemon access warnings fell back to non-daemon compilation and did not fail the build.
  - Evidence: `.\gradlew.bat :app:assembleDebug` output: `BUILD SUCCESSFUL in 14s` with `42 actionable tasks: 3 executed, 39 up-to-date`.
  - Evidence: Direct local Bazel executable build equivalent to `bazel build //app:app` output: `INFO: Build completed successfully, 31 total actions`; outputs included `bazel-bin/app/app_deploy.jar`, `bazel-bin/app/app_unsigned.apk`, and `bazel-bin/app/app.apk`.
  - Evidence: `openspec.cmd validate complete-ui-async-loading --strict` output: `Change 'complete-ui-async-loading' is valid`.
