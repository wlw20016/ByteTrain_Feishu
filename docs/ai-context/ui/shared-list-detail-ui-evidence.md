# Shared List Detail UI Evidence

Date: 2026-06-08

Change: `extract-shared-list-detail-ui`

## Scope

- Added `//shared/ui:ui_android` for shared Android View UI primitives.
- Message and mail lists now call `createSharedPagedListScreen`.
- Message and mail list/detail badge rows now call `createSharedBadgeRow` where applicable.
- Message and mail detail headers now call `createSharedDetailHeader`.
- Shared helpers now own list footer state, bottom-scroll load trigger, scroll restoration, dp conversion, and color parsing.

## Focused UI Checks

Command:

```powershell
rg -n "createSharedPagedListScreen|createSharedLoadMoreFooter|restoreScrollBeforeDraw|createSharedBadgeRow|createSharedDetailHeader" shared\ui features\message\ui features\mail\ui
```

Result: passed. Output showed both message and mail using the shared paged list shell, shared badge row, and shared detail header. Shared footer and scroll restoration were defined and invoked from `shared/ui/SharedListDetailUi.kt`.

Command:

```powershell
rg -n "private fun dp|fun ScrollView.restoreScrollBeforeDraw|private fun createLoadMoreFooter|private fun createBadgeRow|private fun createHeader\(|createConversationHeader" features\message\ui features\mail\ui
```

Result: passed. No duplicated feature-local list footer, scroll restoration, detail header, badge row, or dp helpers remained.

## Gradle Verification

Command:

```powershell
.\gradlew.bat :app:compileDebugKotlin
```

Result: passed. Kotlin daemon connection failed due to local daemon temp-file access, then Gradle fell back to daemonless Kotlin compilation and completed successfully.

Command:

```powershell
.\gradlew.bat :app:assembleDebug
```

Result: passed. Build completed successfully in 13s.

## Bazel Verification

Command:

```powershell
bazel --batch build --curses=no //shared/ui:ui_android //features/message:ui //features/mail:ui //app:app
```

Result: passed after running outside the restricted sandbox. Initial sandbox run returned `Access is denied.`. The elevated run emitted a Bazel Java log-path warning containing non-Latin-1 text, then completed successfully:

```text
INFO: Found 4 targets...
INFO: Build completed successfully, 36 total actions
```

## Diff Hygiene

Command:

```powershell
git diff --check
```

Result: passed. Git reported only CRLF conversion warnings.
