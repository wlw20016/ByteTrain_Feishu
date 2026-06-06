# UI main flow release evidence

This document records REL-001 evidence for the first-stage UI main flow.

## Build verification

```powershell
.\gradlew.bat :app:assembleDebug
```

Expected result: `BUILD SUCCESSFUL`.

## Automated checks

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\checks\ui\check-ui-001.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\ui\check-ui-002.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\ui\check-ui-003.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\message\check-msg-006.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\mail\check-mail-006.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\test\check-test-001.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\test\check-test-002.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\test\check-test-003.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\checks\test\check-test-004.ps1
openspec.cmd validate add-ui-main-flow --strict
```

## Manual acceptance

Manual acceptance checklist:

| Flow | Expected result |
| --- | --- |
| App launch | App opens the first-stage UI shell. |
| Tab switching | Messages and Mail tabs switch the visible list. |
| Message list | First page shows mock conversations. |
| Message load more | `Load more` appends another message page. |
| Message detail | Tapping a conversation opens detail; back returns to the list. |
| Mail list | First page shows mock email cards. |
| Mail load more | `Load more` appends another mail page. |
| Mail detail | Tapping an email opens detail; back returns to the list. |
| 10000 records | Both tabs expose 10000 mock records through paged loading. |

This evidence completes the first-stage UI main-flow acceptance path using the temporary Gradle build entry from BUILD-001.
