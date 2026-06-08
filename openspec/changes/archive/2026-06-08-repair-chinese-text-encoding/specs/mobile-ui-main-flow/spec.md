## ADDED Requirements

### Requirement: User-visible mobile text MUST be readable UTF-8

Android user-visible text for message and mail flows MUST be readable UTF-8 text and MUST NOT contain mojibake artifacts.

#### Scenario: User opens message and mail screens

- Given the app is launched on a device or emulator
- When the user views bottom tabs, list headers, detail headers, content descriptions, and detail body placeholders
- Then Chinese text is readable
- And no mojibake fragments are visible to the user
