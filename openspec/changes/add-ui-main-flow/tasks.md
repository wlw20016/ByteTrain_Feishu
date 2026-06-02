# Tasks: add-ui-main-flow

## 1. App 壳与导航

- [x] UI-001 实现真实 Android `MainActivity`，替换当前占位 class。
  - 证据：`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已继承 Android `Activity`，覆盖 `onCreate` 并安装根视图；`app/src/main/AndroidManifest.xml` 已注册 launcher activity；`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-001.ps1` 通过。
- [x] UI-002 实现消息和邮箱底部双 Tab，并与 `AppRoutes` 保持一致。
  - 证据：`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已基于 `AppRoutes.MESSAGE_LIST` 和 `AppRoutes.MAIL_LIST` 实现底部双 Tab 与内容切换；`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-002.ps1` 通过。

## 2. 临时 Gradle 构建入口

- [x] BUILD-001 新增 Android+Gradle 最小构建入口，支持 `:app:assembleDebug`。
  - 证据：已新增 `settings.gradle.kts`、根 `build.gradle.kts`、`gradle.properties`、`app/build.gradle.kts` 和 Gradle Wrapper；`app` 模块能够编译当前 `MainActivity`、`shared` 和 `features` 源码；`.\gradlew.bat :app:assembleDebug` 执行通过，结果为 `BUILD SUCCESSFUL`。
- [x] BUILD-002 配置 `app` 模块 sourceSets，临时纳入 `shared/` 和 `features/` 源码。
  - 证据：`app/build.gradle.kts` 的 `android.sourceSets.main` 已将 `src/main/kotlin`、`../shared`、`../features` 纳入 `java.srcDirs`；未改变现有包名和模块目录；`.\gradlew.bat :app:assembleDebug` 执行通过，证明当前 `app`、`shared` 和 `features` 源码可由临时单 `:app` Gradle 模块编译。
- [x] BUILD-003 记录 Gradle 到 Bazel 的过渡边界。
  - 证据：`docs/ai-context/build-commands.md` 已新增“Gradle 到 Bazel 的过渡边界”，说明 Gradle 仅用于第一阶段 Android UI 本地编译、打包和运行验证；Bazel rules、targets、query、cache、proto 和 Rust SDK 构建仍由 `wire-bazel-build` change 跟踪；本 change 不完成 Bazel rules 接入。

## 3. 共享 UI 与分页

- [x] UI-003 新增统一 UI 列表/详情模型：`UnifiedListItem`、`AvatarModel`、`BadgeModel`、`DisplayStyle`、`DetailModel`、`DetailMeta`。
  - 证据：`shared/ui/UnifiedUiModels.kt` 已新增统一列表/详情模型，且不依赖 feature 或 Android framework；当前阶段先通过 Gradle 验证，`app/build.gradle.kts` 已临时将 `../shared` 纳入 `:app` main source set，`.\gradlew.bat :app:assembleDebug` 执行通过，证明该模型可被 Android App 编译；`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-003.ps1` 通过；`shared/ui/BUILD.bazel` 保留 `UnifiedUiModels.kt` 源码清单，后续真实 Bazel target 接入仍由 `wire-bazel-build` 跟踪。
- [x] UI-004 扩展 `PagingUiState`，补齐加载更多和加载更多失败状态。
  - 证据：`shared/list/PagingModels.kt` 已将分页状态扩展为 Loading、Empty、Error、Content、LoadingMore、LoadMoreError；`LoadingMore` 和 `LoadMoreError` 均保留当前已加载 `items`，便于列表继续展示旧数据；当前阶段先通过 Gradle 验证，`.\gradlew.bat :app:assembleDebug` 执行通过；`shared/list/BUILD.bazel` 保留 `PagingModels.kt` 源码清单，后续真实 Bazel target 接入仍由 `wire-bazel-build` 跟踪。
- [x] TEST-003 记录并验证 UI 状态矩阵：Loading、Empty、Error、Content、LoadingMore、LoadMoreError。
  - 证据：`docs/ai-context/ui-state-matrix.md` 已记录六种分页 UI 状态的数据条件、UI 预期和恢复路径；`scripts/check-test-003.ps1` 已验证 `PagingUiState` 声明和状态矩阵文档覆盖 Loading、Empty、Error、Content、LoadingMore、LoadMoreError；`powershell -ExecutionPolicy Bypass -File .\scripts\check-test-003.ps1`、`.\gradlew.bat :app:assembleDebug` 和 `openspec validate add-ui-main-flow --strict` 均通过。

## 4. 消息主链路

- [x] MSG-001 扩展 `MessageItem` 字段，包含会话名、会话类型、头像、最后消息摘要/时间、未读数、置顶、免打扰、机器人等字段。
  - 证据：`features/message/domain/MessageItem.kt` 已扩展为会话业务模型，新增 `ConversationType`，并包含会话名、会话类型、头像 URL/文字、最后消息摘要/时间、未读数、置顶、免打扰和机器人字段；`rg "\.title|\.summary|\.timestampMillis|\.unread\b|MessageItem\(" features app shared -n` 仅发现新模型与 mock 构造引用。
- [x] MSG-002 实现 `MockMessageRepository`，生成 10000 条数据并支持 cursor 分页。
  - 证据：`features/message/data/MockMessageRepository.kt` 已实现 `MessageRepository`，默认生成 10000 条 mock 会话数据，并基于字符串 cursor 作为起始下标返回 `MessagePage(items, nextCursor, hasMore)`。
- [x] MSG-003 实现 `MessageItem -> UnifiedListItem` 映射。
  - 证据：`features/message/mapper/MessageUiMapper.kt` 已新增 `MessageItem.toUnifiedListItem()`，将会话名、最后消息摘要、时间、头像、未读/置顶/免打扰/机器人状态和详情元信息映射为 `UnifiedListItem`；`features/message/BUILD.bazel` 保留 `mapper/MessageUiMapper.kt` 源码清单，后续真实 Bazel target 接入仍由 `wire-bazel-build` 跟踪；`powershell -ExecutionPolicy Bypass -File .\scripts\check-msg-003.ps1`、`.\gradlew.bat :app:assembleDebug` 和 `openspec validate add-ui-main-flow --strict` 均通过。
- [x] MSG-004 实现消息列表页面，依赖 `BUILD-002` 提供可编译运行的 Android UI 验证入口。
  - 证据：`features/message/ui/MessageListScreen.kt` 已新增消息列表 View，展示 `UnifiedListItem` 标题、摘要、时间、头像和 badges；`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已在 `AppRoutes.MESSAGE_LIST` 中加载 `MockMessageRepository` 第一页并通过 `MessageItem.toUnifiedListItem()` 渲染消息列表；`features/message/BUILD.bazel` 保留 `ui/MessageListScreen.kt` 源码清单，后续真实 Bazel target 接入仍由 `wire-bazel-build` 跟踪；`powershell -ExecutionPolicy Bypass -File .\scripts\check-msg-004.ps1`、`.\gradlew.bat :app:assembleDebug` 和 `openspec validate add-ui-main-flow --strict` 均通过。
- [x] MSG-005 实现消息列表加载更多。
  - 证据：`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已维护已加载消息、`nextMessageCursor` 和 `hasMoreMessages`，并基于 `MockMessageRepository.loadPage(MESSAGE_PAGE_SIZE, nextMessageCursor)` 追加下一页；`features/message/ui/MessageListScreen.kt` 已新增 `Load more` 页脚按钮和无更多数据提示；`powershell -ExecutionPolicy Bypass -File .\scripts\check-msg-005.ps1`、`.\gradlew.bat :app:assembleDebug` 和 `openspec validate add-ui-main-flow --strict` 均通过。
- [x] MSG-006 实现消息详情页。
  - 证据：`features/message/ui/MessageDetailScreen.kt` 已新增消息详情 View，展示 `UnifiedListItem.detail` 标题、正文和 meta 信息，并提供返回消息列表按钮；`features/message/ui/MessageListScreen.kt` 已支持点击消息行触发详情回调；`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已维护 `selectedMessageItem`，支持消息列表与详情页切换；`features/message/BUILD.bazel` 保留 `ui/MessageDetailScreen.kt` 源码清单；`powershell -ExecutionPolicy Bypass -File .\scripts\check-msg-006.ps1`、`.\gradlew.bat :app:assembleDebug` 和 `openspec validate add-ui-main-flow --strict` 均通过。

## 5. 邮箱主链路

- [x] MAIL-001 扩展 `MailItem` 字段，包含发件人、主题、摘要、接收时间、未读、附件、邮件类型、操作文案等字段。
  - 文档要求：`MailItem` MUST 保留 `id`、`sender`、`subject`、`preview`、`timestampMillis`、`unread` 作为基础字段，并新增附件、邮件类型和操作文案字段，以支撑 QQ 邮箱提醒风格卡片和详情元信息渲染。
  - 验收要求：字段命名 MUST 与 Kotlin 领域模型保持一致；邮件类型 SHOULD 使用枚举表达；附件信息 SHOULD 能区分无附件和有附件数量；操作文案 MUST 能直接映射到 `UnifiedListItem` 的 badge/action 展示。
  - 证据：`features/mail/domain/MailItem.kt` 已保留基础字段，并新增 `attachmentCount`、`mailType`、`actionText` 和 `MailType` 枚举；`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-003.ps1` 通过。
- [x] MAIL-002 实现 `MockMailRepository`，生成 10000 条数据并支持 cursor 分页。
  - 文档要求：`MockMailRepository` MUST 实现 `MailRepository.loadPage(pageSize, cursor)`，默认生成确定性的 10000 条邮件数据，并返回 `MailPage(items, nextCursor, hasMore)`。
  - 验收要求：分页行为 MUST 与 `MockMessageRepository` 保持一致；`cursor == null` 或空字符串表示第一页；合法 cursor 表示起始下标；最后一页 MUST 返回 `hasMore = false` 且 `nextCursor = null`；非法 cursor 不应导致 UI 崩溃。
  - 证据：`features/mail/data/MockMailRepository.kt` 已实现 `MailRepository`，默认生成 10000 条确定性邮件数据，并基于 cursor 返回 `MailPage(items, nextCursor, hasMore)`；`features/mail/BUILD.bazel` 已记录 `data/MockMailRepository.kt`；`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-002.ps1` 通过。
- [x] MAIL-003 实现 `MailItem -> UnifiedListItem` 映射。
  - 证据：`features/mail/mapper/MailUiMapper.kt` 已新增 `MailItem.toUnifiedListItem()`，将主题、摘要、接收时间、发件人头像、未读、附件数量、邮件类型、操作文案和详情元信息映射到 `UnifiedListItem`；`features/mail/BUILD.bazel` 已记录 `mapper/MailUiMapper.kt`；`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-003.ps1` 通过。
- [x] MAIL-004 实现邮箱卡片列表页面，依赖 `BUILD-002` 提供可编译运行的 Android UI 验证入口。
  - 证据：`features/mail/ui/MailListScreen.kt` 已新增邮箱卡片列表 View，渲染 `UnifiedListItem` 标题、摘要、时间、头像和 badges；`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已在 `AppRoutes.MAIL_LIST` 加载邮箱列表并通过 `MailItem.toUnifiedListItem()` 渲染首批预览 mock 邮件；`features/mail/BUILD.bazel` 已记录 `ui/MailListScreen.kt`；`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-004.ps1` 通过。
- [x] MAIL-005 实现邮箱列表加载更多。
  - 证据：`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已维护 `loadedMails`、`nextMailCursor` 和 `hasMoreMails`，并通过 `MockMailRepository.loadPage(MAIL_PAGE_SIZE, nextMailCursor)` 追加下一页；`features/mail/ui/MailListScreen.kt` 已新增 `Load more` 页脚按钮和无更多数据提示；`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-005.ps1` 通过。
- [x] MAIL-006 实现邮箱详情页。
  - 证据：`features/mail/ui/MailDetailScreen.kt` 已新增邮箱详情 View，展示 `UnifiedListItem.detail` 标题、正文和 meta 信息，并提供返回邮箱列表按钮；`features/mail/ui/MailListScreen.kt` 已支持点击邮箱卡片触发详情回调；`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已维护 `selectedMailItem`，支持邮箱列表与详情页切换；`features/mail/BUILD.bazel` 已记录 `ui/MailDetailScreen.kt`；`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-006.ps1` 通过。

## 6. 测试与证据

- [x] TEST-001 添加 repository 分页测试，覆盖第一页、下一页、最后一页、非法 cursor 和空结果。
  - 证据：`docs/ai-context/repository-paging-tests.md` 已记录 Message repository 和 Mail repository 的 First page、Next page、Last page、Invalid cursor、Empty result 测试矩阵；`scripts/check-test-001.ps1` 已验证 `MockMessageRepository` 和 `MockMailRepository` 的 cursor 解析、分页切片、最后一页 `hasMore`/`nextCursor` 和空结果处理；`powershell -ExecutionPolicy Bypass -File .\scripts\check-test-001.ps1` 通过。
- [x] TEST-002 添加 mapper 测试，验证消息和邮箱到统一 UI 模型的关键字段映射。
  - 证据：`docs/ai-context/mapper-field-tests.md` 已记录 Message mapper 和 Mail mapper 到 `UnifiedListItem` 的 `id`、`title`、`subtitle`、`timestampText`、`avatar`、`badges`、`displayStyle` 和 `detail` 字段矩阵；`scripts/check-test-002.ps1` 已验证 `MessageUiMapper.kt` 和 `MailUiMapper.kt` 的关键字段映射；`powershell -ExecutionPolicy Bypass -File .\scripts\check-test-002.ps1` 通过。
- [ ] TEST-004 验证 10000 条数据滚动表现，并记录人工验收证据。
- [ ] REL-001 完成第一阶段 UI 主链路验收，并在本文件记录证据；依赖 `BUILD-001` 提供 Gradle 构建或运行验证结果。

## 7. 文档

- [ ] DOC-001 记录本 UI change 的 AI prompt、AI 结论、人工决策和最终结果。
- [ ] 更新飞书多维表格中 UI 相关任务的状态、负责人和证据链接。
