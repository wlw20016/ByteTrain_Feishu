# Change: Extract Shared List Detail UI

## Why

Message and mail already map their domain models into shared `UnifiedListItem` and `DetailModel` types. The current UI still duplicates list scaffolding, load-more footer behavior, scroll restoration, header patterns, badge rendering, and detail navigation patterns across message and mail screens. The requirement says message and mail should reuse the same list, pagination, and detail abstractions; the current implementation only partially satisfies that at the model layer.

## What Changes

- Add shared Android View builders for paged list shells, rows/cards where practical, headers, load-more footers, badge rows, and detail scaffolding.
- Keep feature-specific visual styling through configuration objects or rendering callbacks.
- Update message and mail screens to delegate common list/detail behavior to shared UI code.
- Preserve current product behavior and navigation.

## Impact

- Affects `shared/ui` and message/mail UI modules.
- Requires Bazel target dependency updates so feature UI modules can depend on the shared Android UI abstraction.
- No domain, proto, or SDK contract changes.
