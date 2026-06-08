# Scroll performance evidence

This document records TEST-004 evidence for the first UI main-flow phase.

Automated check:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\checks\test\check-test-004.ps1
```

## Data volume

Both list data sources expose 10000 deterministic records:

- Message repository: `MockMessageRepository.DEFAULT_TOTAL_COUNT = 10_000`
- Mail repository: `MockMailRepository.DEFAULT_TOTAL_COUNT = 10_000`

## Paging behavior

The app does not render all 10000 records at once. `MainActivity` uses a screen-sized page for both tabs:

- Messages: visible message rows plus a small preload buffer.
- Mail: visible mail cards plus a small preload buffer.

Each list appends the next screen-sized page automatically after the user scrolls near the `上滑加载更多` footer.

## Manual acceptance

Manual acceptance checklist for 10000-record scrolling:

| Area | Expected result |
| --- | --- |
| Messages | First page renders quickly with a screen-sized page of conversations. |
| Messages | Scrolling near the bottom appends another screen-sized page without replacing existing rows. |
| Messages | Header reports `已显示 {items.size} / 10000 个会话`. |
| Mail | First page renders quickly with a screen-sized page of email cards. |
| Mail | Scrolling near the bottom appends another screen-sized page without replacing existing cards. |
| Mail | Header reports `已显示 {items.size} / 10000 封邮件`. |

The current first-stage UI uses simple Android `ScrollView` screens and page-based appends. This is acceptable for the staged mock UI because only loaded pages are rendered, while a future production UI can migrate to recycler-style rendering when needed.
