# Mobile UI Main Flow Delta

## ADDED Requirements

### Requirement: The app SHALL provide two primary tabs

The Android app SHALL provide Message and Mail tabs as the primary navigation surface.

#### Scenario: User switches tabs

- Given the app is launched
- When the user selects the Message or Mail tab
- Then the app displays the corresponding list without losing the other tab's navigation contract

### Requirement: Message tab SHALL support paged mock conversations

The Message tab SHALL render Feishu-style conversation items from a paged data source containing 10000 mock records.

#### Scenario: User loads more messages

- Given the message list has more records
- When the user scrolls to the load-more threshold
- Then the next page is loaded through the repository and appended to the list

### Requirement: Mail tab SHALL support paged mock mail cards

The Mail tab SHALL render QQ-mail-reminder-style mail cards from a paged data source containing 10000 mock records.

#### Scenario: User loads more mails

- Given the mail list has more records
- When the user scrolls to the load-more threshold
- Then the next page is loaded through the repository and appended to the list

### Requirement: List and detail UI SHALL reuse shared models

Message and mail UI SHALL map business models into shared list/detail UI models before rendering.

#### Scenario: A detail page is opened

- Given a user taps a message or mail item
- When the detail page opens
- Then it renders from `DetailModel` rather than directly depending on feature-specific rendering logic

### Requirement: Paging UI states SHALL be explicit

The UI SHALL represent loading, empty, error, content, loading-more, and load-more-error states explicitly.

#### Scenario: Loading more fails

- Given the list already has content
- When loading the next page fails
- Then the existing content remains visible and the load-more error state is displayed with retry behavior

