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

### Requirement: Message detail MUST behave like a mobile chat view

The message detail screen MUST present conversation content as a chat-style view and MUST avoid exposing internal mock/debug metadata.

#### Scenario: User opens a message detail

- Given the user taps a message conversation
- When the detail screen opens
- Then the content is shown as chat bubbles with a composer area
- And internal fields such as type, unread count, pinned state, muted state, and bot state are not shown as meta rows
- And there is no in-page `Back to messages` button

### Requirement: Detail navigation MUST use Android system back and preserve list position

Message and mail detail screens MUST rely on Android system back navigation for returning to the previous list. Returning from a detail screen MUST restore the list scroll position the user had before opening the detail.

#### Scenario: User returns from message detail

- Given the user has scrolled the message list and opened a message detail
- When the user presses the Android system back navigation
- Then the app returns to the message list
- And the message list restores the previous scroll position instead of jumping to the first item

#### Scenario: User returns from mail detail

- Given the user has scrolled the mail list and opened a mail detail
- When the user presses the Android system back navigation
- Then the app returns to the mail list
- And the mail list restores the previous scroll position instead of jumping to the first item
