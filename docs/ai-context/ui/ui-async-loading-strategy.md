# UI async loading strategy

This document records the implementation strategy for `complete-ui-async-loading`.

## Previous blocking path

`MainActivity` previously called `MessageRepository.loadPage` and `MailRepository.loadPage` through a local `runSuspendBlocking` helper. That helper started the suspend block and then waited on a `CountDownLatch`, so first-page and next-page reads could block the UI caller until repository work finished.

The affected paths were:

- `ensureInitialMessagesLoaded -> loadInitialMessagePage -> runSuspendBlocking`
- `onLoadMore -> loadNextMessagePage -> runSuspendBlocking`
- `ensureInitialMailsLoaded -> loadInitialMailPage -> runSuspendBlocking`
- `onLoadMore -> loadNextMailPage -> runSuspendBlocking`

## Chosen async strategy

The app does not currently carry a coroutine dependency, so `MainActivity` uses Android/JDK primitives already available to the app:

- a single background `ExecutorService` starts repository suspend calls away from the UI call path,
- `startCoroutine` launches the suspend repository block without a latch or synchronous wait,
- `Handler(Looper.getMainLooper())` posts success and failure transitions back to the main thread,
- `onDestroy` removes pending callbacks and shuts down the executor.

Message and mail loading keep separate first-page and next-page in-flight flags. Duplicate load-more triggers return while `isLoadingMoreMessages` or `isLoadingMoreMails` is true.

## UI state flow

Both tabs store a `PagingUiState` in `MainActivity` and pass the mapped state into their list screen:

- first-page start: `Loading`
- first-page success with items: `Content`
- first-page success without items: `Empty`
- first-page failure: `Error`
- next-page start: `LoadingMore`, preserving existing items
- next-page failure: `LoadMoreError`, preserving existing items

The list screens render these states directly. Scroll restoration still uses the existing `initialScrollY` and pre-draw restore path, including after asynchronous append completion.
