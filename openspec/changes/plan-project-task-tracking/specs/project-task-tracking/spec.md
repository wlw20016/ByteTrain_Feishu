# Project Task Tracking Delta

## ADDED Requirements

### Requirement: Project tasks SHALL map to repository evidence

Every P0 or P1 project task SHALL be traceable from the task table to an OpenSpec change and repository output.

#### Scenario: A tracked task is completed

- Given a task is marked completed in Feishu Bitable
- When the reviewer opens the task's OpenSpec change
- Then the matching `tasks.md` item includes implementation, build, test, or manual validation evidence

### Requirement: Feishu Bitable SHALL be operational, not authoritative

Feishu Bitable SHALL be used for ownership and progress tracking, while OpenSpec remains the authoritative evidence record.

#### Scenario: Status differs between Bitable and OpenSpec

- Given a Bitable row says a task is done
- When the related OpenSpec task has no evidence
- Then the task is not accepted for project review

