# Mobile UI Main Flow Polish

## ADDED Requirements

### Requirement: Bottom navigation MUST use icon tabs

The Android app bottom navigation MUST render the message and mail destinations as custom icon-based tabs instead of native text-heavy buttons.

#### Scenario: User sees icon navigation

- Given the app has launched
- When the bottom navigation is visible
- Then the message destination is represented by a message icon
- And the mail destination is represented by a mail icon
- And each destination includes a compact label below its icon
- And each icon tab retains an accessible label for assistive technologies

#### Scenario: User changes selected tab

- Given the app is showing the message or mail list
- When the user selects the other bottom tab
- Then the selected tab icon and label use the selected color
- And the unselected tab icon and label use the inactive color

### Requirement: Mock conversation names MUST avoid visible numeric suffixes

Message mock data MUST use natural conversation names without appending visible row numbers to the displayed title.

#### Scenario: User sees message list mock data

- Given the message list has loaded mock conversations
- When conversation titles are rendered
- Then names such as `Mia Zhang`, `Noah Liu`, and `Calendar Bot` appear without trailing generated numbers
- And record identity remains stable through the item `id` field rather than the visible title
