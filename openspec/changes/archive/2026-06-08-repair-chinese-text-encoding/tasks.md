# Tasks

- [x] TEXT-ENC-001 Inventory mojibake in user-visible Android UI strings and current OpenSpec/docs files.
  - Evidence: Initial default PowerShell reads displayed false-positive mojibake, so files were re-read with explicit UTF-8. Current UI strings, active OpenSpec specs, `openspec/project.md`, and `docs/ai-context` were readable UTF-8; remaining acceptance work was user-visible English text in message/mail labels and missing automated mojibake coverage.
- [x] TEXT-ENC-002 Repair message and mail UI text literals, labels, and content descriptions.
  - Evidence: Normalized user-visible message/mail screen text in `MessageListScreen.kt`, `MailListScreen.kt`, `MessageUiMapper.kt`, `MailUiMapper.kt`, `MockMailRepository.kt`, and `MainActivity.kt`; synchronized focused message/mail checks with the readable Chinese UI labels.
- [x] TEXT-ENC-003 Repair current specs and AI-context documents needed for acceptance review.
  - Evidence: Verified current OpenSpec specs and `docs/ai-context` with explicit UTF-8 reads; no current mojibake remained. Updated UI AI-context evidence to match the readable Chinese load-more and header labels.
- [x] TEXT-ENC-004 Add a focused encoding/mojibake check script with documented exclusions for archived history if needed.
  - Evidence: Added `scripts/checks/docs/check-text-encoding.ps1`; it scans source, scripts, docs, active OpenSpec files, and current specs while excluding archived OpenSpec history.
- [x] TEXT-ENC-005 Update docs to state UTF-8 expectations for future AI and manual edits.
  - Evidence: Updated `docs/README.md` and `docs/ai-context/ui/ui-main-flow-doc-001.md` with UTF-8 editing expectations and the focused encoding check command.
- [x] TEXT-ENC-006 Run encoding checks, Gradle app build, and relevant Bazel build; record evidence.
  - Evidence: `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\docs\check-text-encoding.ps1` passed; `check-msg-005.ps1`, `check-mail-004.ps1`, `check-mail-005.ps1`, and `check-doc-001.ps1` passed; `openspec.cmd validate repair-chinese-text-encoding --strict` passed; `.\gradlew.bat :app:assembleDebug` passed with `BUILD SUCCESSFUL`.
  - Bazel evidence: `C:\Users\23064\.bazelisk\downloads\sha256\2db62663b47eb90143932cd553f66840dc03da8e7ce0a23a1302e63fdc234254\bin\bazel.exe --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4` was attempted and failed before analysis with `FATAL: Output base directory 'C:\Users\23064\_bazel_23064\ftf4zhtr' must be readable and writable.` A workspace-local output root could start Bazel but failed fetching `protobuf-33.4.bazel.tar.gz` due to network timeout, so no source build failure was observed.
