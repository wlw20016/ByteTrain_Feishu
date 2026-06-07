# Change: Fix Load More Scroll Jump

## Why

When message or mail lists auto-load the next page near the bottom, the current implementation rebuilds the list once for the loading footer and again after appending data. The rebuilt `ScrollView` is visible at the top before its saved scroll position is restored, so the page briefly jumps upward and then returns to the previous position.

## What Changes

- Preserve the user's current scroll position when load-more is triggered.
- Append the next page before rerendering the list, avoiding the intermediate loading-state rerender that exposes a top-positioned `ScrollView`.
- Restore saved scroll position before the first draw of the rebuilt list.
- Update regression checks for message and mail load-more behavior.

## Impact

- Affects message and mail list pagination UI.
- Keeps existing Gradle/Bazel source layout unchanged.
- No repository contract changes.
