# mobile-ui-main-flow Specification

## Purpose
TBD - created by archiving change polish-ui-navigation-names. Update Purpose after archive.
## Requirements
### Requirement: Bottom navigation MUST use icon tabs

The Android app bottom navigation MUST render the message and mail destinations as custom icon-based tabs instead of native text-heavy buttons.

#### Scenario: User sees icon navigation

- Given the app has launched
- When the bottom navigation is visible
- Then the message destination is represented by a message icon
- And the mail destination is represented by a mail icon
- And each destination includes a compact label below its icon
- And each icon tab retains an accessible label for assistive technologies

#### Scenario: User changes selected tab

- Given the app is showing the message or mail list
- When the user selects the other bottom tab
- Then the selected tab icon and label use the selected color
- And the unselected tab icon and label use the inactive color

### Requirement: Mock conversation names MUST avoid visible numeric suffixes

Message mock data MUST use natural conversation names without appending visible row numbers to the displayed title.

#### Scenario: User sees message list mock data

- Given the message list has loaded mock conversations
- When conversation titles are rendered
- Then names such as `Mia Zhang`, `Noah Liu`, and `Calendar Bot` appear without trailing generated numbers
- And record identity remains stable through the item `id` field rather than the visible title

### Requirement: Message detail MUST behave like a mobile chat view

The message detail screen MUST present conversation content as a chat-style view and MUST avoid exposing internal mock/debug metadata.

#### Scenario: User opens a message detail

- Given the user taps a message conversation
- When the detail screen opens
- Then the content is shown as chat bubbles with a composer area
- And internal fields such as type, unread count, pinned state, muted state, and bot state are not shown as meta rows
- And there is no in-page `Back to messages` button
- And the chat header provides a compact back affordance instead of a full-width back button

### Requirement: Detail navigation MUST use Android system back and preserve list position

Message and mail detail screens MUST rely on Android system back navigation for returning to the previous list. Returning from a detail screen MUST restore the list scroll position the user had before opening the detail.

#### Scenario: User returns from message detail

- Given the user has scrolled the message list and opened a message detail
- When the user presses the Android system back navigation
- Then the app returns to the message list
- And the message list restores the previous scroll position instead of jumping to the first item

#### Scenario: User returns from mail detail

- Given the user has scrolled the mail list and opened a mail detail
- When the user presses the Android system back navigation
- Then the app returns to the mail list
- And the mail list restores the previous scroll position instead of jumping to the first item

### Requirement: Mail detail MUST use a mobile email reading view

The mail detail screen MUST present email content as a mobile reading view and MUST avoid exposing internal mock/debug metadata.

#### Scenario: User opens a mail detail

- Given the user taps a mail card
- When the detail screen opens
- Then the content is shown as an email reading page with a compact header back affordance
- And the sender, time, subject, preview body, and useful badges are presented naturally
- And internal fields such as raw type, unread state, attachment count, and action labels are not shown as debug-style meta rows
- And there is no in-page `Back to mail` button

### Requirement: Lists MUST auto-load more content when scrolled near the bottom

Message and mail lists MUST behave like mobile app feeds: loading more content is triggered by scrolling near the bottom instead of tapping a full-width `Load more` button. When a next page is appended, the list MUST preserve the user's visible position without briefly rendering from the top.

#### Scenario: User scrolls to the end of a list

- Given the message or mail list has more mock data
- When the user scrolls near the bottom of the list
- Then the app shows a loading-more footer state
- And the next page is appended automatically
- And the list remains near the user's current position after new content is appended instead of jumping to the top

#### Scenario: Load-more rerender preserves position before drawing

- Given the message or mail list is scrolled near the bottom
- When auto-load appends the next page and rerenders the list
- Then the rebuilt list restores the saved scroll position before the first draw
- And the user does not see a temporary jump to the first item

#### Scenario: Page size follows visible screen capacity

- Given the app is running on a device or emulator with a specific list viewport height
- When the message or mail list requests an initial or next page
- Then the repository page size is calculated from the number of rows that can fit in the visible list area
- And the calculated page size includes a small preload buffer
- And the calculated page size is clamped to the repository page-size limit

### Requirement: App MUST 提供两个主 Tab

Android App MUST 提供消息和邮箱两个 Tab，作为核心导航入口。

#### Scenario: 用户切换 Tab

- Given App 已启动
- When 用户选择消息或邮箱 Tab
- Then App 展示对应列表，并保持两个 Tab 的导航契约稳定

### Requirement: UI 主链路 MUST 提供临时 Gradle 构建入口

在完整 Bazel 接入完成前，Android UI 主链路 MUST 提供一个临时 Gradle 构建入口，用于本地编译和运行验证。

#### Scenario: 开发者构建调试 APK

- Given 项目已新增 Gradle 构建入口
- When 开发者执行 `:app:assembleDebug`
- Then Gradle 构建能够编译 `app`、`shared` 和 `features` 中的当前 Android/Kotlin 源码

### Requirement: 消息 Tab MUST 支持分页 mock 会话

消息 Tab MUST 从包含 10000 条 mock 记录的分页数据源渲染飞书风格会话列表。

#### Scenario: 用户加载更多消息

- Given 消息列表还有更多记录
- When 用户滚动到加载更多阈值
- Then 下一页数据通过 repository 加载并追加到列表

### Requirement: 邮箱 Tab MUST 支持分页 mock 邮件卡片

邮箱 Tab MUST 从包含 10000 条 mock 记录的分页数据源渲染 QQ 邮箱提醒风格邮件卡片。

#### Scenario: 用户加载更多邮件

- Given 邮箱列表还有更多记录
- When 用户滚动到加载更多阈值
- Then 下一页数据通过 repository 加载并追加到列表

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

### Requirement: 分页 UI 状态 MUST 显式表达

UI MUST 显式表达加载、空态、错误、内容、加载更多和加载更多失败状态。

#### Scenario: 加载更多失败

- Given 列表已经有内容
- When 下一页加载失败
- Then 现有内容保持可见，并展示可重试的加载更多失败状态

### Requirement: 邮箱领域模型 MUST 覆盖卡片和详情所需字段

邮箱主链路的 Kotlin `MailItem` MUST 覆盖列表卡片和详情页渲染所需字段，包括发件人、主题、摘要、接收时间、未读、附件、邮件类型和操作文案。

#### Scenario: 邮件字段映射到共享 UI 模型

- Given 一条包含附件、邮件类型和操作文案的邮件领域模型
- When mapper 将 `MailItem` 转换为 `UnifiedListItem`
- Then 共享 UI 模型保留标题、摘要、时间、未读状态、附件提示、类型提示和操作文案所需信息

### Requirement: 邮箱 mock repository MUST 提供确定性 cursor 分页

邮箱主链路的 mock 数据源 MUST 默认提供 10000 条确定性邮件数据，并通过 `MailRepository.loadPage(pageSize, cursor)` 返回 cursor 分页结果。

#### Scenario: 请求邮箱最后一页

- Given 邮箱 mock repository 已加载到最后一页 cursor
- When UI 请求下一页邮件
- Then repository 返回最后一批邮件，`hasMore` 为 `false`，并且 `nextCursor` 为 `null`

### Requirement: Paging UI state MUST explicitly represent loading flows

UI MUST explicitly represent loading, empty, error, content, loading-more, and load-more-error states. Page reads from the UI layer MUST be launched asynchronously and MUST NOT block the UI call path with a synchronous wait helper.

#### Scenario: First page loads asynchronously

- Given the user opens the message or mail tab with no loaded content
- When the app requests the first page
- Then the UI enters a first-page loading state
- And the page request runs asynchronously
- And the UI does not use a blocking suspend bridge to wait for the result

#### Scenario: Next page loads asynchronously

- Given the list already has loaded content and `hasMore` is true
- When the user scrolls near the bottom
- Then the UI enters a loading-more state while preserving the existing items
- And the next page request runs asynchronously
- And duplicate load-more triggers are ignored until the in-flight request completes

#### Scenario: Async failure preserves recoverable state

- Given a first-page or next-page request fails
- When the failure is reported back to the UI
- Then first-page failure is rendered as an error state
- And next-page failure is rendered as load-more-error while preserving already loaded items

### Requirement: User-visible mobile text MUST be readable UTF-8

Android user-visible text for message and mail flows MUST be readable UTF-8 text and MUST NOT contain mojibake artifacts.

#### Scenario: User opens message and mail screens

- Given the app is launched on a device or emulator
- When the user views bottom tabs, list headers, detail headers, content descriptions, and detail body placeholders
- Then Chinese text is readable
- And no mojibake fragments are visible to the user

