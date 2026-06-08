# Design: Repair Chinese Text Encoding

## Current State

Files such as `openspec/project.md`, parts of `openspec/specs/mobile-ui-main-flow/spec.md`, and some Android detail screen literals contain mojibake. Some English docs are readable and should not be churned.

## Approach

- Prioritize files that are part of current acceptance:
  - user-visible strings in message/mail detail and navigation,
  - active OpenSpec specs,
  - `openspec/project.md`,
  - AI context docs used for review and troubleshooting.
- Replace mojibake with clear Simplified Chinese or plain English where that better matches the surrounding file.
- Keep technical identifiers unchanged.
- Add a focused PowerShell check for common mojibake fragments by Unicode code point, including U+951B, U+7ECB, U+9435, U+5A11, U+95AD, U+9225, and the replacement character.
- Exempt archived historical files only if repairing them would create excessive churn; document any explicit exclusions.

## Validation

- Focused mojibake check passes for source files and required current docs/specs.
- Gradle/Bazel app build still passes after UI string changes.
- AI context docs remain readable and linked from `docs/README.md`.
