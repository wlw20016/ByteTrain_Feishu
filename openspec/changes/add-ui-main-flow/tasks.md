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
  - 证据：`shared/ui/UnifiedUiModels.kt` 已新增统一列表/详情模型，且不依赖 feature 或 Android framework；`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-003.ps1` 通过。
- [ ] UI-004 扩展 `PagingUiState`，补齐加载更多和加载更多失败状态。
- [ ] TEST-003 记录并验证 UI 状态矩阵：Loading、Empty、Error、Content、LoadingMore、LoadMoreError。

## 4. 消息主链路

- [x] MSG-001 扩展 `MessageItem` 字段，包含会话名、会话类型、头像、最后消息摘要/时间、未读数、置顶、免打扰、机器人等字段。
  - 证据：`features/message/domain/MessageItem.kt` 已扩展为会话业务模型，新增 `ConversationType`，并包含会话名、会话类型、头像 URL/文字、最后消息摘要/时间、未读数、置顶、免打扰和机器人字段；`rg "\.title|\.summary|\.timestampMillis|\.unread\b|MessageItem\(" features app shared -n` 仅发现新模型与 mock 构造引用。
- [x] MSG-002 实现 `MockMessageRepository`，生成 10000 条数据并支持 cursor 分页。
  - 证据：`features/message/data/MockMessageRepository.kt` 已实现 `MessageRepository`，默认生成 10000 条 mock 会话数据，并基于字符串 cursor 作为起始下标返回 `MessagePage(items, nextCursor, hasMore)`。
- [ ] MSG-003 实现 `MessageItem -> UnifiedListItem` 映射。
- [ ] MSG-004 实现消息列表页面，依赖 `BUILD-002` 提供可编译运行的 Android UI 验证入口。
- [ ] MSG-005 实现消息列表加载更多。
- [ ] MSG-006 实现消息详情页。

## 5. 邮箱主链路

- [ ] MAIL-001 扩展 `MailItem` 字段，包含发件人、主题、摘要、接收时间、未读、附件、邮件类型、操作文案等字段。
- [ ] MAIL-002 实现 `MockMailRepository`，生成 10000 条数据并支持 cursor 分页。
- [ ] MAIL-003 实现 `MailItem -> UnifiedListItem` 映射。
- [ ] MAIL-004 实现邮箱卡片列表页面，依赖 `BUILD-002` 提供可编译运行的 Android UI 验证入口。
- [ ] MAIL-005 实现邮箱列表加载更多。
- [ ] MAIL-006 实现邮箱详情页。

## 6. 测试与证据

- [ ] TEST-001 添加 repository 分页测试，覆盖第一页、下一页、最后一页、非法 cursor 和空结果。
- [ ] TEST-002 添加 mapper 测试，验证消息和邮箱到统一 UI 模型的关键字段映射。
- [ ] TEST-004 验证 10000 条数据滚动表现，并记录人工验收证据。
- [ ] REL-001 完成第一阶段 UI 主链路验收，并在本文件记录证据；依赖 `BUILD-001` 提供 Gradle 构建或运行验证结果。

## 7. 文档

- [ ] DOC-001 记录本 UI change 的 AI prompt、AI 结论、人工决策和最终结果。
- [ ] 更新飞书多维表格中 UI 相关任务的状态、负责人和证据链接。
