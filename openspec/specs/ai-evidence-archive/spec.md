# ai-evidence-archive Specification

## Purpose
TBD - created by archiving change complete-ai-evidence-archive. Update Purpose after archive.
## Requirements
### Requirement: Final AI evidence must be stored in the repository

The project MUST store final AI evidence in repository files, including prompts, context, AI conclusions, human decisions, accepted/rejected suggestions, and final results.

#### Scenario: Mentor reviews AI participation

- **WHEN** a reviewer opens the repository without reading chat history
- **THEN** the reviewer can trace major AI-assisted decisions and outcomes from OpenSpec and `docs/ai-context` files

### Requirement: Completed P0 tasks must have local evidence

Every completed P0 task MUST have evidence in its owning OpenSpec `tasks.md` or an explicitly linked local document.

#### Scenario: Completed task audit

- **WHEN** completed P0 tasks are audited
- **THEN** each completed task links to code, document, build/test command, or manual acceptance evidence

### Requirement: Completed changes must be validated before archive

An OpenSpec change MUST pass strict validation and have complete task evidence before it is archived into long-term specs.

#### Scenario: Archive completed change

- **WHEN** a change is selected for archive
- **THEN** strict validation passes and the archive record preserves the accepted requirements and evidence summary

