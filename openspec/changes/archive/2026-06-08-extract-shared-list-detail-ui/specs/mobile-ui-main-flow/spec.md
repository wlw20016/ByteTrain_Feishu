## MODIFIED Requirements

### Requirement: 列表和详情 UI MUST 复用共享模型

Message and mail UI MUST reuse shared list, pagination, footer, badge, scroll-restoration, and applicable detail primitives in addition to shared UI data models. Feature screens MAY keep product-specific row/card and detail body rendering when the visual interaction differs.

#### Scenario: Message and mail reuse the shared list shell

- Given message and mail lists both render paged `UnifiedListItem` content
- When the list screens are built
- Then both screens use the same shared list shell for scroll container, load-more trigger, footer state, and scroll restoration
- And feature-specific row or card rendering is supplied through configuration or callbacks

#### Scenario: Shared detail primitives are reused

- Given message and mail detail pages both need compact navigation headers
- When the detail screens are built
- Then both screens use shared header/back affordance primitives
- And message chat content and mail reading content remain feature-specific where their layout semantics differ
