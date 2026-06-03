# Tasks: polish-ui-navigation-names

## 1. UI Polish

- [x] UI-005 Replace bottom navigation text buttons with message/mail icons, while preserving accessible labels.
  - Evidence: `MainActivity` now renders custom `LinearLayout` tabs with `ImageView` icons and compact `TextView` labels, keeps `contentDescription = label`, and leaves route switching on `AppRoutes.MESSAGE_LIST` / `AppRoutes.MAIL_LIST`.
- [x] UI-007 Add selected-state feedback to bottom navigation tabs.
  - Evidence: `MainActivity.applyTabSelection` updates selected and unselected icon/text colors and applies a light selected background to the active tab.
- [x] UI-006 Remove visible numeric suffixes from mock message conversation names.
  - Evidence: `MockMessageRepository.conversationNameFor` now returns the selected single, group, or bot name directly; stable uniqueness remains in `MessageItem.id`.

## 2. Verification

- [x] TEST-005 Add and run a focused script that verifies icon navigation resources, custom tab rendering, selected-state feedback, accessibility labels, and mock conversation name formatting.
  - Evidence: `powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-005.ps1`, `cmd /c openspec validate polish-ui-navigation-names --strict`, and `.\gradlew.bat :app:assembleDebug` all passed.
