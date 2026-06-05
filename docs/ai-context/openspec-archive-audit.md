# OpenSpec Validate And Archive Audit

记录日期：2026-06-05。

本文档记录 `complete-ai-evidence-archive` 阶段的 strict validate、superseded 判断和 archive 决策。

## Strict validate summary

命令从仓库根目录运行。结果如下：

| Change | Status before audit | Strict validate |
| --- | --- | --- |
| `verify-final-bazel-delivery` | complete 13/13 | valid |
| `prototype-ide-build-plugin` | complete 11/11 | valid |
| `connect-sdk-protobuf-runtime` | complete 12/12 | valid |
| `wire-bazel-build` | complete 11/11 | valid |
| `polish-ui-navigation-names` | complete 10/10 | valid |
| `plan-project-task-tracking` | complete 12/12 | valid |
| `init-project-architecture` | in-progress 8/9 before final Bazel evidence closeout | valid |
| `improve-ai-context` | in-progress before DOC/REL closeout | valid |
| `add-ui-main-flow` | in-progress due optional Feishu table task | valid |
| `add-sdk-contract` | in-progress due optional Feishu table task | valid |
| `complete-ai-evidence-archive` | in-progress during audit | valid |

Validated commands:

```powershell
openspec validate verify-final-bazel-delivery --strict
openspec validate prototype-ide-build-plugin --strict
openspec validate connect-sdk-protobuf-runtime --strict
openspec validate wire-bazel-build --strict
openspec validate polish-ui-navigation-names --strict
openspec validate plan-project-task-tracking --strict
openspec validate init-project-architecture --strict
openspec validate improve-ai-context --strict
openspec validate add-ui-main-flow --strict
openspec validate add-sdk-contract --strict
openspec validate complete-ai-evidence-archive --strict
```

All commands returned `Change '<name>' is valid`.

## Superseded decisions

| Change / task | Decision | Reason |
| --- | --- | --- |
| `init-project-architecture` final Bazel verification task | Close as completed by `verify-final-bazel-delivery` | The final app/proto/shared-feature/Rust/query Bazel evidence now exists in `docs/ai-context/final-bazel-delivery-evidence.md`. |
| `improve-ai-context` DOC-001 / prompt update / P0 evidence audit | Close as completed by `complete-ai-evidence-archive` | Final prompt, AI evidence, P0 audit and evidence links are now stored locally. |
| `improve-ai-context` DOC-002 / DOC-003 | Close as completed by current final docs | `project-structure.md`, `module-boundaries.md`, `build-commands.md`, `common-build-errors.md` match the final Android-only Bazel state. |
| `improve-ai-context` REL-002 | Close as completed by this archive audit | Completed changes are validated and archive decisions are recorded here. |
| `add-ui-main-flow` Feishu table task | Leave for optional fifth stage | External table updates are not formal local evidence. |
| `add-sdk-contract` Feishu table task | Leave for optional fifth stage | External table updates are not formal local evidence. |
| `improve-ai-context` Feishu final evidence link task | Leave for optional fifth stage | External table updates are optional and non-blocking. |

## Archive candidates

The following changes met archive conditions after validation and evidence audit and were archived:

- `plan-project-task-tracking`
- `init-project-architecture`
- `polish-ui-navigation-names`
- `wire-bazel-build`
- `connect-sdk-protobuf-runtime`
- `prototype-ide-build-plugin`
- `verify-final-bazel-delivery`
- `complete-ai-evidence-archive`

The following changes remain active or deferred:

- `add-ui-main-flow`: formal implementation is complete, but an optional Feishu table task remains for phase five.
- `add-sdk-contract`: formal implementation is complete, but an optional Feishu table task remains for phase five.
- `improve-ai-context`: remains active until phase-five optional Feishu link task is handled or explicitly marked non-required.

## Archive result

Executed archive commands:

```powershell
openspec archive plan-project-task-tracking --yes
openspec archive init-project-architecture --yes
openspec archive polish-ui-navigation-names --yes
openspec archive wire-bazel-build --yes
openspec archive connect-sdk-protobuf-runtime --yes
openspec archive prototype-ide-build-plugin --yes
openspec archive verify-final-bazel-delivery --yes
openspec validate complete-ai-evidence-archive --strict
openspec archive complete-ai-evidence-archive --yes
```

Archived changes:

- `openspec/changes/archive/2026-06-05-plan-project-task-tracking`
- `openspec/changes/archive/2026-06-05-init-project-architecture`
- `openspec/changes/archive/2026-06-05-polish-ui-navigation-names`
- `openspec/changes/archive/2026-06-05-wire-bazel-build`
- `openspec/changes/archive/2026-06-05-connect-sdk-protobuf-runtime`
- `openspec/changes/archive/2026-06-05-prototype-ide-build-plugin`
- `openspec/changes/archive/2026-06-05-verify-final-bazel-delivery`
- `openspec/changes/archive/2026-06-05-complete-ai-evidence-archive`

Specs now present:

- `openspec/specs/project-architecture/spec.md`
- `openspec/specs/project-task-tracking/spec.md`
- `openspec/specs/mobile-ui-main-flow/spec.md`
- `openspec/specs/bazel-build/spec.md`
- `openspec/specs/sdk-runtime-integration/spec.md`
- `openspec/specs/ide-build-plugin/spec.md`
- `openspec/specs/bazel-delivery-verification/spec.md`
- `openspec/specs/ai-evidence-archive/spec.md`

Archive notes:

- `init-project-architecture` initially failed archive because the existing `project-architecture` spec lacked `## Purpose` and English `MUST/SHALL` keywords. The spec format was corrected without changing the requirement meaning, then archive succeeded.
- Proposal warnings about older proposals missing `## Why` / `## What Changes` were non-blocking and did not prevent archive.
- Active changes after archive: `improve-ai-context`, `add-ui-main-flow`, and `add-sdk-contract`. These remain only because phase-five optional Feishu table/link tasks are intentionally deferred outside formal local evidence.

Final validation:

```powershell
openspec validate --all --strict
```

Result: `11 passed, 0 failed`.
