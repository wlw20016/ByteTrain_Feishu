# Tasks

- [x] UI-LOAD-001 Document the load-more scroll-jump root cause and expected behavior.
  - Evidence: `openspec/changes/fix-load-more-scroll-jump/proposal.md` and `design.md` document the intermediate rerender root cause and the pre-draw restoration fix.
- [x] UI-LOAD-002 Add regression checks for single-rerender load-more and pre-draw scroll restoration.
  - Evidence: `scripts/checks/message/check-msg-005.ps1`, `scripts/checks/mail/check-mail-005.ps1`, and `scripts/checks/ui/check-ui-011.ps1` now fail the old `render -> post -> append -> render` pattern and require pre-draw scroll restoration.
- [x] UI-LOAD-003 Update message and mail load-more flow to preserve visible position without jumping to top.
  - Evidence: `MainActivity` now appends the next page before the single rerender, and both list screens restore `initialScrollY` through `ViewTreeObserver.OnPreDrawListener`.
- [x] UI-LOAD-004 Run OpenSpec validation, focused regression checks, and Gradle debug build.
  - Evidence: `openspec validate fix-load-more-scroll-jump --strict`, `check-msg-005.ps1`, `check-mail-005.ps1`, `check-ui-011.ps1`, and `.\gradlew.bat :app:assembleDebug` all pass.
- [x] UI-LOAD-005 Replace fixed 30-row message/mail page sizes with screen-sized page sizes.
  - Evidence: `MainActivity` now calls `messagePageSize()` and `mailPageSize()` for initial and next-page repository loads. Both functions calculate screen-visible capacity from the list viewport and clamp it to repository bounds.
- [x] UI-LOAD-006 Update scroll performance evidence and regression checks for dynamic page sizing.
  - Evidence: `docs/ai-context/ui/scroll-performance-evidence.md` documents screen-sized pages, and `powershell -ExecutionPolicy Bypass -File .\scripts\checks\test\check-test-004.ps1` verifies dynamic page-size calculation.
