# AI Context Delta

## ADDED Requirements

### Requirement: AI collaboration evidence SHALL be recorded

Important AI-assisted project work SHALL record prompt context, AI conclusions, human decisions, and final results.

#### Scenario: A feature is implemented with AI assistance

- Given AI helped generate design, code, tests, or troubleshooting suggestions
- When the feature task is marked complete
- Then the related OpenSpec evidence includes what AI suggested and what humans accepted or rejected

### Requirement: AI-readable engineering docs SHALL stay current

The project SHALL maintain AI-readable docs for structure, module boundaries, build commands, common errors, and IDE workflow.

#### Scenario: A future AI assistant diagnoses a build issue

- Given the assistant reads `docs/ai-context`
- When it analyzes a build failure
- Then it can use current module boundaries, commands, and known-error records to reason about the issue

### Requirement: IDE and Bazel collaboration SHALL be documented

The project SHALL document how Trae or VS Code works with Bazel commands and project indexing.

#### Scenario: A teammate sets up the project

- Given the teammate follows the IDE/Bazel workflow document
- When they run the documented commands
- Then they can edit, build, and diagnose the project without relying on chat history

