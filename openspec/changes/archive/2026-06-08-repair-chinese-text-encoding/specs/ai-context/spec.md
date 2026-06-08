## MODIFIED Requirements

### Requirement: AI 可读工程文档 MUST 保持更新

Project AI context, current OpenSpec specs, and review-facing docs MUST be stored as readable UTF-8 text. They MUST avoid mojibake so future AI assistants and human reviewers can consume the context without relying on chat history.

#### Scenario: AI assistant reads project context

- Given an AI assistant reads current specs and `docs/ai-context`
- When it parses requirements, build commands, module boundaries, and known issues
- Then the text is readable UTF-8
- And known historical encoding exclusions are documented if any archived files are intentionally left unchanged
