# Design: improve-ai-context

## Documentation Surfaces

Maintain these AI-readable documents:

- `docs/ai-context/project-structure.md`
- `docs/ai-context/module-boundaries.md`
- `docs/ai-context/build-commands.md`
- `docs/ai-context/common-build-errors.md`
- `docs/ai-context/ide-bazel-workflow.md`
- `openspec/prompt.md`

## Evidence Model

Each important AI-assisted task records:

- Prompt
- Context given to AI
- AI conclusion
- Human decision
- Accepted/rejected suggestions
- Final result
- Build/test/manual validation evidence when applicable

## IDE Workflow

The IDE workflow document should explain how the team edits, indexes, builds, tests, and diagnoses Kotlin, Rust, proto, and BUILD files in Trae or VS Code.

## Build Helper

The plugin/helper task is P2. If time is limited, a documented design and minimal command wrapper are acceptable before a full plugin.
