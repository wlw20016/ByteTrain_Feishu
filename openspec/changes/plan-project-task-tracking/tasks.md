# Tasks: plan-project-task-tracking

## 1. Feishu Bitable Setup

- [ ] Import `C:\Users\23064\feishu_app_bitable_tasks.tsv` into Feishu Bitable.
- [ ] Configure `Status`, `Priority`, and `Owner` as structured fields.
- [ ] Create views for P0 tasks, owner workload, blocked tasks, and acceptance-ready tasks.

## 2. OpenSpec Mapping

- [x] Create phase-level OpenSpec changes for the generated task list.
  - Evidence: `plan-project-task-tracking`, `add-ui-main-flow`, `add-sdk-contract`, `wire-bazel-build`, `improve-ai-context`.
- [ ] Add the OpenSpec change ID to each Feishu Bitable task row.
- [ ] Keep Bitable task IDs in related `tasks.md` items.

## 3. Review Workflow

- [ ] Define that Bitable status is operational tracking only.
- [ ] Define that OpenSpec `tasks.md` evidence is the formal acceptance source.
- [ ] Update PR template or PR description convention to include OpenSpec change ID, test result, and AI usage notes.

## 4. Acceptance

- [ ] Every P0 task is linked to an OpenSpec change.
- [ ] Every completed P0 task has evidence in the corresponding `tasks.md`.
- [ ] The final project review can be traced from Bitable row -> OpenSpec change -> code/doc diff -> build/test/manual evidence.
