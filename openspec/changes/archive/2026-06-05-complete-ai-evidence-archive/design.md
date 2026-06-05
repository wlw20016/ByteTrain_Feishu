## Overview

This change closes the documentation and process side of the course requirements. It does not add product UI behavior; it proves that the repository contains the required AI/OpenSpec evidence chain.

## Evidence Model

Each major phase should have evidence for:

- prompt
- context supplied to AI
- AI conclusion or suggestion
- human decision
- accepted and rejected suggestions
- final result
- build/test/manual verification evidence

## Audit Scope

Audit these major phase changes:

- `init-project-architecture`
- `plan-project-task-tracking`
- `add-ui-main-flow`
- `polish-ui-navigation-names`
- `wire-bazel-build`
- `add-sdk-contract`
- `improve-ai-context`
- new finalization changes created after the course gap review

## Documentation Targets

Update the AI-readable documents that support future assistant work:

- `docs/ai-context/project-structure.md`
- `docs/ai-context/module-boundaries.md`
- `docs/ai-context/build-commands.md`
- `docs/ai-context/common-build-errors.md`
- `docs/ai-context/ide-bazel-workflow.md`
- `openspec/prompt.md`

## Archive Strategy

Archive only changes that meet these conditions:

- all tasks complete or explicitly superseded by a new change
- `openspec validate <change> --strict` passes
- build/test/manual evidence is recorded locally
- long-term requirements have been merged into `openspec/specs`

Changes still blocked by runtime SDK, final Bazel verification, or plugin work should remain active until those changes are complete.

## Retrospective

Create a concise final retrospective that lists:

- reusable prompts
- AI suggestions accepted
- AI suggestions rejected
- build failures diagnosed with AI help
- remaining limitations
