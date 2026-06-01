# Tasks: add-ui-main-flow

## 1. App 壳与导航

- [x] UI-001 实现真实 Android `MainActivity`，替换当前占位 class。
  - 证据：`app/src/main/kotlin/com/bytetrain/feishuclone/MainActivity.kt` 已继承 Android `Activity`，覆盖 `onCreate` 并安装根视图；`app/src/main/AndroidManifest.xml` 已注册 launcher activity；`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-001.ps1` 通过。
- [ ] UI-002 实现消息和邮箱底部双 Tab，并与 `AppRoutes` 保持一致。

## 2. 共享 UI 与分页

- [ ] UI-003 新增统一 UI 列表/详情模型：`UnifiedListItem`、`AvatarModel`、`BadgeModel`、`DisplayStyle`、`DetailModel`、`DetailMeta`。
- [ ] UI-004 扩展 `PagingUiState`，补齐加载更多和加载更多失败状态。
- [ ] TEST-003 记录并验证 UI 状态矩阵：Loading、Empty、Error、Content、LoadingMore、LoadMoreError。

## 3. 消息主链路

- [ ] MSG-001 扩展 `MessageItem` 字段，包含会话名、会话类型、头像、最后消息摘要/时间、未读数、置顶、免打扰、机器人等字段。
- [ ] MSG-002 实现 `MockMessageRepository`，生成 10000 条数据并支持 cursor 分页。
- [ ] MSG-003 实现 `MessageItem -> UnifiedListItem` 映射。
- [ ] MSG-004 实现消息列表页面。
- [ ] MSG-005 实现消息列表加载更多。
- [ ] MSG-006 实现消息详情页。

## 4. 邮箱主链路

- [ ] MAIL-001 扩展 `MailItem` 字段，包含发件人、主题、摘要、接收时间、未读、附件、邮件类型、操作文案等字段。
- [ ] MAIL-002 实现 `MockMailRepository`，生成 10000 条数据并支持 cursor 分页。
- [ ] MAIL-003 实现 `MailItem -> UnifiedListItem` 映射。
- [ ] MAIL-004 实现邮箱卡片列表页面。
- [ ] MAIL-005 实现邮箱列表加载更多。
- [ ] MAIL-006 实现邮箱详情页。

## 5. 测试与证据

- [ ] TEST-001 添加 repository 分页测试，覆盖第一页、下一页、最后一页、非法 cursor 和空结果。
- [ ] TEST-002 添加 mapper 测试，验证消息和邮箱到统一 UI 模型的关键字段映射。
- [ ] TEST-004 验证 10000 条数据滚动表现，并记录人工验收证据。
- [ ] REL-001 完成第一阶段 UI 主链路验收，并在本文件记录证据。

## 6. 文档

- [ ] DOC-001 记录本 UI change 的 AI prompt、AI 结论、人工决策和最终结果。
- [ ] 更新飞书多维表格中 UI 相关任务的状态、负责人和证据链接。
