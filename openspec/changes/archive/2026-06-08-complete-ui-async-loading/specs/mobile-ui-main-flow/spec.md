## ADDED Requirements

### Requirement: Paging UI state MUST explicitly represent loading flows

UI MUST explicitly represent loading, empty, error, content, loading-more, and load-more-error states. Page reads from the UI layer MUST be launched asynchronously and MUST NOT block the UI call path with a synchronous wait helper.

#### Scenario: First page loads asynchronously

- Given the user opens the message or mail tab with no loaded content
- When the app requests the first page
- Then the UI enters a first-page loading state
- And the page request runs asynchronously
- And the UI does not use a blocking suspend bridge to wait for the result

#### Scenario: Next page loads asynchronously

- Given the list already has loaded content and `hasMore` is true
- When the user scrolls near the bottom
- Then the UI enters a loading-more state while preserving the existing items
- And the next page request runs asynchronously
- And duplicate load-more triggers are ignored until the in-flight request completes

#### Scenario: Async failure preserves recoverable state

- Given a first-page or next-page request fails
- When the failure is reported back to the UI
- Then first-page failure is rendered as an error state
- And next-page failure is rendered as load-more-error while preserving already loaded items
