# UI main flow DOC-001 record

This document records the DOC-001 evidence for the `add-ui-main-flow` change.

## AI prompt

The UI main-flow work was driven by prompts in `openspec/prompt.md` and follow-up implementation requests in the development thread. The recurring user goals were:

- Build a Feishu-like Android App with two tabs: Messages and Mail.
- Provide 10000 mock records for both tabs.
- Support cursor pagination, `Load more`, list rendering, detail navigation, and return-to-list behavior.
- Reuse shared list/detail UI models across Messages and Mail.
- Keep OpenSpec tasks updated with implementation, build, test, and manual acceptance evidence.
- Use the temporary Gradle entry for first-stage Android UI verification while Bazel integration remains tracked separately.

## AI conclusions

The implementation followed these AI-assisted conclusions:

- First-stage UI should use a temporary single `:app` Gradle module so Android screens can be compiled and accepted early.
- Feature modules should keep independent domain models and map them into shared `UnifiedListItem` and `DetailModel` structures.
- Pagination should be repository-driven through `loadPage(pageSize, cursor)` rather than rendering all 10000 records at once.
- Message and Mail flows should use parallel structure: domain model, mock repository, mapper, list screen, detail screen, and verification scripts.
- Test evidence should be recorded as PowerShell checks and AI-readable Markdown matrices until full Kotlin/JUnit or Android UI test infrastructure is introduced.
- SDK-backed repository adapters should sit behind repository interfaces, preserving the Kotlin mock repositories as fallback until native SDK integration is ready.

## Human decisions

The human-directed decisions for this change were:

- Prioritize the visible main flow over final Bazel target wiring.
- Accept temporary Gradle validation for the first UI phase.
- Keep Messages and Mail as separate feature modules while sharing UI models.
- Let Mail extend its Kotlin domain model with UI-specific fields such as `attachmentCount`, `mailType`, and `actionText`.
- Treat OpenSpec `tasks.md` evidence as the formal acceptance record.
- Record manual acceptance evidence in repository docs rather than relying only on external tracking tools.
- Keep SDK adapter work at the repository boundary instead of pretending that native Rust FFI is already integrated.

## Final result

The `add-ui-main-flow` change now delivers the first-stage Android UI main flow:

- App shell with Messages and Mail tabs.
- Message list, `Load more`, and detail page.
- Mail list, `Load more`, and detail page.
- Shared `UnifiedListItem`, avatar, badge, display style, and detail models.
- Message and Mail mock repositories with 10000 deterministic records and cursor pagination.
- Mapper evidence for both feature models into shared UI models.
- Repository paging evidence and UI state evidence.
- 10000-record scroll-performance evidence.
- Release acceptance evidence for the first UI main flow.

Primary verification commands:

```powershell
.\gradlew.bat :app:assembleDebug
openspec.cmd validate add-ui-main-flow --strict
powershell -ExecutionPolicy Bypass -File .\scripts\checks\release\check-rel-001.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\docs\check-doc-001.ps1
```

Related evidence documents:

- `docs/ai-context/build-system/build-commands.md`
- `docs/ai-context/tests/repository-paging-tests.md`
- `docs/ai-context/tests/mapper-field-tests.md`
- `docs/ai-context/ui/ui-state-matrix.md`
- `docs/ai-context/ui/scroll-performance-evidence.md`
- `docs/ai-context/ui/ui-main-flow-release-evidence.md`
- `docs/ai-context/sdk/sdk-adapter-evidence.md`
