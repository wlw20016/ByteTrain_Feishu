# Repository paging test matrix

This document records the TEST-001 repository paging coverage for the first UI main-flow phase.

The automated acceptance script is:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\check-test-001.ps1
```

## Scope

The matrix covers both repository implementations used by the Android UI:

- Message repository: `features/message/data/MockMessageRepository.kt`
- Mail repository: `features/mail/data/MockMailRepository.kt`

Both repositories expose `loadPage(pageSize, cursor)` and return page objects with:

- `items`
- `nextCursor`
- `hasMore`

## Cases

| Case | Input | Expected result |
| --- | --- | --- |
| First page | `pageSize > 0`, `cursor = null` | Returns items from index `0`; when more data exists, `nextCursor` is the end index and `hasMore = true`. |
| Next page | `pageSize > 0`, `cursor` is a valid start index | Returns items from the cursor index; `nextCursor` advances by page size when more data exists. |
| Last page | `cursor` points near the end of the data set | Returns the final items; `nextCursor = null`; `hasMore = false`. |
| Invalid cursor | `cursor` is not numeric | Falls back to index `0` through `toIntOrNull() ?: 0`; UI does not crash. |
| Empty result | `pageSize <= 0` or `totalCount <= 0` | Returns an empty item list; `nextCursor = null`; `hasMore = false`. |

## Evidence

`scripts/check-test-001.ps1` verifies that both repositories implement the shared pagination shape:

- invalid page size and empty data handling
- nullable cursor parsing
- cursor clamping
- page slicing with `subList(startIndex, endIndex)`
- final-page `hasMore` calculation
- final-page `nextCursor = null`

This keeps the repository paging behavior aligned for message and mail before broader test infrastructure is introduced.
