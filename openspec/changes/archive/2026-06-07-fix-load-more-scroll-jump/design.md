# Design: Fix Load More Scroll Jump

## Root Cause

The load-more callback currently does this:

1. Save `scrollY`.
2. Set loading state.
3. Rerender the list.
4. Post a deferred page load.
5. Rerender the list again.

The first rerender creates a new `ScrollView`. Because scroll restoration is posted after layout, the user can see the new list at `scrollY = 0` before it is restored, which causes the visible jitter.

## Approach

- Keep the load-more callback scroll-aware.
- Load and append the next page before the single final rerender.
- Keep the loading flag as a re-entrancy guard.
- Replace `post { scrollTo(...) }` with a pre-draw listener so the restored scroll position is applied before the rebuilt list is presented.
- Calculate message and mail page sizes from the current list viewport height divided by each list's estimated row height, plus a small preload buffer. Clamp the result to the SDK/repository page-size bounds.

## Validation

- Message and mail MSG/MAIL-005 checks verify auto-load behavior and pre-draw scroll restoration.
- TEST-004 verifies page sizes are screen-sized instead of fixed at 30.
- UI-011 verifies both lists do not rely on the old intermediate rerender pattern.
- Gradle debug build verifies Android/Kotlin compilation.
