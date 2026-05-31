# Design: add-ui-main-flow

## 范围

本 change 交付第一阶段可见 App 主链路。Rust SDK 和完整 Bazel 接入不阻塞本 change，它们分别由 `add-sdk-contract` 和 `wire-bazel-build` 跟踪。

## 架构关系

```text
app
  -> shared/navigation
  -> features/message
  -> features/mail
features/message, features/mail
  -> shared/list
  -> shared/ui models
```

## UI 模型

新增统一 UI 模型，让消息和邮箱复用列表与详情渲染：

- `UnifiedListItem`
- `AvatarModel`
- `BadgeModel`
- `DisplayStyle`
- `DetailModel`
- `DetailMeta`

消息和邮箱保留各自业务模型，再映射为统一 UI 模型：

- `MessageItem -> UnifiedListItem`
- `MailItem -> UnifiedListItem`

## 分页状态

扩展现有 `PagingUiState<T>`，覆盖以下状态：

- Loading
- Empty
- Error
- Content
- LoadingMore
- LoadMoreError

分页逻辑不能一次性渲染全部 10000 条数据。Repository 暴露 `loadPage(pageSize, cursor)`，返回 `items`、`nextCursor` 和 `hasMore`。

## 页面

- `MainActivity`：App 入口和根组件。
- 消息 Tab：飞书风格高密度会话列表。
- 消息详情：标题、摘要或正文、时间、未读、置顶、免打扰、机器人等元信息。
- 邮箱 Tab：QQ 邮箱提醒风格卡片列表。
- 邮箱详情：主题、发件人、摘要或正文、接收时间、附件、类型等元信息。

## 验收策略

- Repository 测试覆盖分页边界。
- Mapper 测试覆盖关键字段映射。
- 人工验收覆盖 Tab 切换、首屏加载、加载更多、详情跳转和返回行为。
- 如果 UI 测试基础设施暂未就绪，则在 OpenSpec `tasks.md` 中记录截图或人工验收说明。
