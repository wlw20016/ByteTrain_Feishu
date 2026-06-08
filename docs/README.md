# Documentation index

The documentation tree is organized by purpose:

- `project/`: stable project structure, module boundaries, team workflow, and original assignment notes.
- `ai-context/build/`: build commands, IDE workflow, and recurring build errors.
- `ai-context/sdk/`: SDK adapter evidence and Rust async/protobuf contract notes.
- `ai-context/ui/`: UI state, release evidence, and UI flow notes.
- `ai-context/tests/`: repository paging and mapper field test notes.
- `evidence/`: final delivery evidence, audits, and retrospective documents.

Use `docs/project/project-structure.md` as the main architecture entry and
`docs/ai-context/build-system/build-commands.md` as the build command entry.

## Text encoding

All source, OpenSpec, and documentation files that contain user-visible or review-facing text must be edited and saved as UTF-8. Do not paste Chinese text through tools that reinterpret UTF-8 bytes as a local ANSI code page.

Before marking text-heavy UI or docs work complete, run:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\checks\docs\check-text-encoding.ps1
```

The check intentionally excludes archived OpenSpec history under `openspec/changes/archive/`; current specs, active changes, source files, and `docs/ai-context` must remain readable.
