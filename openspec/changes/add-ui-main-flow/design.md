# Design: add-ui-main-flow

## 范围

本 change 交付第一阶段可见 App 主链路。Rust SDK 和完整 Bazel 接入不阻塞本 change，它们分别由 `add-sdk-contract` 和 `wire-bazel-build` 跟踪。

为支持第一阶段快速运行和验收 Android UI，本 change 允许新增临时 Android+Gradle 构建入口。Gradle 只承担 `:app:assembleDebug` 和本地运行验证职责，不替代 Bazel 目标规划；Bazel rules、targets、query 和构建证据仍由 `wire-bazel-build` 负责。

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

## 临时 Gradle 构建

第一阶段先采用单 `:app` Gradle 模块，降低 Android UI 跑通成本：

- 根目录新增 `settings.gradle.kts`、`build.gradle.kts` 和 `gradle.properties`。
- `app/build.gradle.kts` 使用 Android application 和 Kotlin Android 插件。
- `app` 模块的 main source set 临时纳入 `app/src/main/kotlin`、`../shared` 和 `../features`。
- 后续模块边界稳定后，再拆分为独立 Gradle modules 或迁移为 Bazel targets。

该方案的约束：

- 不改变现有包名、目录结构和 OpenSpec change 归属。
- `shared` 仍不能依赖具体 feature。
- feature 模块不能反向依赖 `app`。
- Bazel 占位文件保留，具体 Bazel 接入仍在 `wire-bazel-build` 中推进。

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
