# Scroll performance evidence

This document records TEST-004 evidence for the first UI main-flow phase.

Automated check:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-test-004.ps1
```

## Data volume

Both list data sources expose 10000 deterministic records:

- Message repository: `MockMessageRepository.DEFAULT_TOTAL_COUNT = 10_000`
- Mail repository: `MockMailRepository.DEFAULT_TOTAL_COUNT = 10_000`

## Paging behavior

The app does not render all 10000 records at once. `MainActivity` uses page size 30 for both tabs:

- Messages: page size 30
- Mail: page size 30

Each list appends the next page only after the user chooses `Load more`.

## Manual acceptance

Manual acceptance checklist for 10000-record scrolling:

| Area | Expected result |
| --- | --- |
| Messages | First page renders quickly with 30 conversations. |
| Messages | `Load more` appends another page without replacing existing rows. |
| Messages | Header reports `Showing {items.size} of 10000 mock conversations`. |
| Mail | First page renders quickly with 30 email cards. |
| Mail | `Load more` appends another page without replacing existing cards. |
| Mail | Header reports `Showing {items.size} of 10000 mock emails`. |

The current first-stage UI uses simple Android `ScrollView` screens and page-based appends. This is acceptable for the staged mock UI because only loaded pages are rendered, while a future production UI can migrate to recycler-style rendering when needed.
