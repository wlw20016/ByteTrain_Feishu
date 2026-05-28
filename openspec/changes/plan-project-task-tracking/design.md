# Design: plan-project-task-tracking

## Tracking Model

Feishu Bitable is used as the operational board. OpenSpec remains the source of reviewable evidence in the repository.

Recommended Bitable fields:

- Task ID
- Phase
- Module
- Task Name
- Description
- Priority
- Owner
- Status
- Estimate
- Dependencies
- Acceptance Criteria
- OpenSpec Change
- Repository Output
- Build or Validation Command

## Change Mapping

- `plan-project-task-tracking`: task registry and tracking workflow.
- `add-ui-main-flow`: Android UI main path, mock data, list/detail, state matrix.
- `add-sdk-contract`: proto, Rust SDK mock, async/data boundary.
- `wire-bazel-build`: Bazel rules, targets, query, build evidence.
- `improve-ai-context`: AI prompt evidence, troubleshooting docs, IDE workflow.

## Workflow

1. Create or update a Feishu Bitable row for each task.
2. Link each P0/P1 row to an OpenSpec change ID.
3. Before implementation, update the relevant `tasks.md`.
4. After implementation, record PR/build/test/manual validation evidence in `tasks.md`.
5. Archive the OpenSpec change after acceptance.

## Human Decision

The task table is optimized for daily tracking. OpenSpec is optimized for review and long-term evidence. A task is only considered accepted when the matching OpenSpec task has evidence, not merely when a Bitable status changes.
