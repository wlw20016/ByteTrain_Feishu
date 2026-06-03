# Mobile UI Main Flow Polish

## ADDED Requirements

### Requirement: Bottom navigation MUST use icon tabs

The Android app bottom navigation MUST render the message and mail destinations as icon-based tabs instead of text-heavy buttons.

#### Scenario: User sees icon navigation

- Given the app has launched
- When the bottom navigation is visible
- Then the message destination is represented by a message icon
- And the mail destination is represented by a mail icon
- And each icon tab retains an accessible label for assistive technologies

### Requirement: Mock conversation names MUST avoid visible numeric suffixes

Message mock data MUST use natural conversation names without appending visible row numbers to the displayed title.

#### Scenario: User sees message list mock data

- Given the message list has loaded mock conversations
- When conversation titles are rendered
- Then names such as `Mia Zhang`, `Noah Liu`, and `Calendar Bot` appear without trailing generated numbers
- And record identity remains stable through the item `id` field rather than the visible title
