# Project Architecture Delta

## ADDED Requirements

### Requirement: Repository architecture MUST follow the recommended module layout

The repository MUST separate app entry, feature modules, shared abstractions, protobuf contracts, Rust SDK code, OpenSpec documents, and AI-readable context documents.

#### Scenario: AI reads the project structure

- Given an AI assistant needs to understand the repository
- When it opens `docs/ai-context/project-structure.md`
- Then it can identify each top-level module and its responsibility

### Requirement: Prompt evidence MUST be stored in OpenSpec

Important AI prompts, AI conclusions, human decisions, and final results MUST be recorded in OpenSpec documents or related repository evidence.

#### Scenario: A feature task is implemented

- Given a P0 feature task has been implemented
- When the task is marked complete
- Then the related `tasks.md` item includes PR, build, test, manual validation, or decision evidence