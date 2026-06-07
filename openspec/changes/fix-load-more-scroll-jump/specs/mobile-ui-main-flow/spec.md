## MODIFIED Requirements

### Requirement: Lists MUST auto-load more content when scrolled near the bottom

Message and mail lists MUST behave like mobile app feeds: loading more content is triggered by scrolling near the bottom instead of tapping a full-width `Load more` button. When a next page is appended, the list MUST preserve the user's visible position without briefly rendering from the top.

#### Scenario: User scrolls to the end of a list

- Given the message or mail list has more mock data
- When the user scrolls near the bottom of the list
- Then the app shows a loading-more footer state
- And the next page is appended automatically
- And the list remains near the user's current position after new content is appended instead of jumping to the top

#### Scenario: Load-more rerender preserves position before drawing

- Given the message or mail list is scrolled near the bottom
- When auto-load appends the next page and rerenders the list
- Then the rebuilt list restores the saved scroll position before the first draw
- And the user does not see a temporary jump to the first item

#### Scenario: Page size follows visible screen capacity

- Given the app is running on a device or emulator with a specific list viewport height
- When the message or mail list requests an initial or next page
- Then the repository page size is calculated from the number of rows that can fit in the visible list area
- And the calculated page size includes a small preload buffer
- And the calculated page size is clamped to the repository page-size limit
