# Proposal: plan-project-task-tracking

## Why

The project has been initialized, but the next work must be trackable across two engineers, Feishu Bitable, OpenSpec changes, build evidence, and acceptance records. A task table alone is not enough for final review because the course requires repository-based evidence.

## What

- Establish the project delivery task registry as an OpenSpec-tracked change.
- Map Feishu Bitable task IDs to OpenSpec changes and repository outputs.
- Define the fields that must be maintained for status, owner, priority, dependency, validation, and evidence.
- Use this change as the entry point for project management tasks, while implementation work lives in dedicated changes.

## Impact

- Adds a project-management OpenSpec change.
- Does not change app behavior or build configuration.
- Creates a stable reference for Feishu Bitable tracking and later PR descriptions.
