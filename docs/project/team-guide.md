# 组内统一开发文档

## 1. 文档目标

本文档用于统一本组在训练营项目中的功能设计、技术路线、分工方式和日常开发流程。

本项目不是单纯完成一个 Android Demo，而是一次围绕 Android App、SDK、protobuf、Bazel、OpenSpec 和 AI 协作的工程化实践。组内开发时需要同时关注：

- App 是否能完成消息和邮箱两个核心页面；
- SDK 接口和数据模型是否清晰；
- 每个功能是否有设计、任务拆分和验收记录；
- AI 的参与过程是否有记录；
- 后续是否能逐步接入 Bazel、Rust SDK 和构建排障文档。

第一阶段以跑通功能主链路为主，不让 Rust、Bazel 和 IDE 插件能力阻塞页面开发。

## 2. 项目功能范围

最终 App 是一个仿飞书风格的 Android App，包含两个核心 Tab：

- 消息 Tab；
- 邮箱 Tab。

两个页面都需要支持：

- 展示大量 mock 数据，目标规模为 10000 条；
- 分页或增量加载；
- 加载态、空态、错误态；
- 点击列表项进入详情页；
- 数据通过 repository / SDK 接口异步获取，而不是直接写死在 UI 中。

## 3. 总体技术路线

组内采用 **契约先行，UI 与 SDK mock 并行** 的路线。

整体阶段如下：

1. 共同确定数据模型、分页模型和接口契约。
2. 前端侧使用 Kotlin mock repository 跑通 UI 主链路。
3. 后端侧根据同一套契约定义 proto 和 SDK mock。
4. UI 从本地 mock repository 替换为 SDK 数据源。
5. 后续接入 Rust SDK，实现真正的 SDK 异步数据提供。
6. 后续接入 Bazel，管理 App、UI 模块、SDK、proto 和测试。
7. 沉淀 AI 可读工程上下文、构建过程 skill 和常见排障记录。

该路线的原则是：

- 先稳定前后端协作边界；
- 先让 App 页面和交互可见；
- proto、SDK、Rust、Bazel 逐步接入；
- 避免一开始同时处理 Android、Rust、protobuf、Bazel 和 IDE 协同导致进度失控。

## 4. 组内分工

本组按类前后端分离方式协作。

### 4.1 前端侧职责

负责人：前端背景成员。

主要负责：

- Android App 页面实现；
- Jetpack Compose UI；
- 消息 Tab 和邮箱 Tab；
- 通用分页列表组件；
- ViewModel 和页面状态管理；
- Loading / Empty / Error / Content / LoadingMore 等 UI 状态；
- 点击列表项进入详情页；
- 接入 repository / SDK 接口并展示数据；
- UI 侧测试和交互验收。

### 4.2 后端侧职责

负责人：Java 后端背景成员。

主要负责：

- 数据模型设计；
- proto 契约定义；
- SDK 接口设计；
- mock 数据生成；
- SDK mock 实现；
- 后续 Rust SDK 实现；
- SDK 错误模型；
- Bazel target 接入；
- 构建日志、失败原因和修复记录沉淀。

### 4.3 共同职责

两位成员共同负责：

- OpenSpec change 创建和维护；
- 需求拆解；
- 技术方案评审；
- 接口契约评审；
- 模块边界确认；
- AI 输出内容的人工审查；
- PR review；
- 最终验收记录。

## 5. 功能设计

### 5.1 消息 Tab

消息 Tab 参考飞书消息列表风格，采用高密度会话列表。

列表项包含联系人、群组和机器人会话。每个列表项建议包含：

- `id`：会话唯一标识；
- `conversationName`：联系人名、群组名或系统会话名；
- `conversationType`：`single`、`group`、`bot`；
- `avatarUrl` 或 `avatarText`：头像图片或文字占位；
- `lastMessagePreview`：最后一条消息摘要；
- `lastMessageTime`：最后消息时间；
- `unreadCount`：未读数量；
- `isPinned`：是否置顶；
- `isMuted`：是否免打扰；
- `isBot`：是否机器人会话。

展示结构：

- 左侧展示头像；
- 中间上方展示联系人名或群组名；
- 中间下方展示最后消息摘要；
- 右侧展示时间和未读数；
- 机器人会话可以展示“机器人”标签。

点击消息列表项后进入消息详情页。第一阶段详情页可以直接使用列表项已有字段展示，不额外增加复杂详情接口。

### 5.2 邮箱 Tab

邮箱 Tab 参考 QQ 邮箱提醒风格，采用卡片式邮件提醒列表。

每封邮件展示为一个卡片。每个邮件项建议包含：

- `id`：邮件唯一标识；
- `senderName`：发件人或公司名称；
- `subject`：邮件主题；
- `preview`：邮件摘要；
- `receivedTime`：接收时间；
- `isUnread`：是否未读；
- `hasAttachment`：是否有附件；
- `mailType`：`normal`、`notification`、`interview`、`system`；
- `actionText`：卡片底部操作文案，默认可以为“邮件详情”。

展示结构：

- 卡片顶部展示发件人或公司名称；
- 中部展示主题和摘要；
- 底部展示“邮件详情”入口；
- 时间可以作为卡片之间的分隔信息，或展示在卡片右上角；
- 未读邮件可以通过标题加粗或小圆点体现。

点击邮件卡片后进入邮件详情页。第一阶段详情页可以复用邮件列表项字段展示。

## 6. 数据模型与接口设计

本项目要求消息页和邮箱页复用同一套列表、分页和详情页抽象，并且 UI 数据模型也要可复用。

因此数据模型采用 **业务模型 + UI 统一模型** 的两层设计：

```text
SDK / proto 业务模型
MessageItem / MailItem
        ↓ mapper
UI 统一模型
UnifiedListItem / DetailModel
        ↓
通用列表、分页和详情页组件
```

这样可以同时满足两个目标：

- SDK 和 proto 层保留消息、邮箱各自的业务字段；
- UI 层只消费统一模型，从而复用列表、分页状态和详情页抽象。

### 6.1 SDK / proto 业务模型

消息和邮箱的业务数据存在差异，因此底层业务模型不强行合并。

消息业务模型建议包含：

```text
MessageItem
- id: String
- conversationName: String
- conversationType: single / group / bot
- avatarUrl: String?
- lastMessagePreview: String
- lastMessageTime: Long
- unreadCount: Int
- isPinned: Boolean
- isMuted: Boolean
- isBot: Boolean
```

邮箱业务模型建议包含：

```text
MailItem
- id: String
- senderName: String
- subject: String
- preview: String
- receivedTime: Long
- isUnread: Boolean
- hasAttachment: Boolean
- mailType: normal / notification / interview / system
```

### 6.2 统一分页模型

消息和邮箱复用同一套分页返回结构：

```text
PageResult<T>
- items: List<T>
- nextCursor: String?
- hasMore: Boolean
```

分页请求参数：

```text
pageSize: Int
cursor: String?
```

约定：

- `cursor` 为空时表示加载第一页；
- `nextCursor` 为空且 `hasMore = false` 表示没有更多数据；
- SDK 接口必须异步返回；
- 第一阶段可以由 Kotlin mock repository 实现；
- 后续由 SDK mock、Rust SDK 逐步替换。

### 6.3 UI 统一列表模型

UI 层不直接依赖 `MessageItem` 或 `MailItem`，而是统一消费 `UnifiedListItem`。

建议模型：

```text
UnifiedListItem
- id: String
- title: String
- subtitle: String
- preview: String
- timeText: String
- avatar: AvatarModel?
- badges: List<BadgeModel>
- unreadCount: Int
- isUnread: Boolean
- isPinned: Boolean
- displayStyle: DisplayStyle
- detail: DetailModel
```

展示样式通过 `displayStyle` 区分：

```text
DisplayStyle
- CompactConversation  // 消息页：飞书式高密度会话列表
- MailCard             // 邮箱页：QQ 邮箱式卡片列表
```

头像模型：

```text
AvatarModel
- imageUrl: String?
- text: String?
- backgroundColor: String?
```

标签模型：

```text
BadgeModel
- text: String
- type: BadgeType

BadgeType
- Bot
- Group
- Attachment
- System
- Important
```

### 6.4 统一详情页模型

详情页也复用统一模型，不直接依赖消息或邮箱业务模型。

```text
DetailModel
- id: String
- title: String
- subtitle: String
- body: String
- timeText: String
- metadata: List<DetailMeta>

DetailMeta
- label: String
- value: String
```

消息详情可以映射为：

```text
title = 会话名称
subtitle = 最后一条消息时间
body = 消息内容或摘要
metadata = 会话类型、未读数、是否置顶、是否免打扰
```

邮件详情可以映射为：

```text
title = 邮件主题
subtitle = 发件人或公司名称
body = 邮件摘要或正文
metadata = 接收时间、是否有附件、邮件类型
```

### 6.5 业务模型到 UI 模型的映射

消息和邮箱分别提供 mapper，将业务模型转换为 UI 统一模型。

```text
MessageItem -> UnifiedListItem
MailItem -> UnifiedListItem
```

示例映射关系：

```text
MessageItem.conversationName      -> UnifiedListItem.title
MessageItem.conversationType      -> UnifiedListItem.subtitle / badges
MessageItem.lastMessagePreview    -> UnifiedListItem.preview
MessageItem.lastMessageTime       -> UnifiedListItem.timeText
MessageItem.unreadCount           -> UnifiedListItem.unreadCount / isUnread
MessageItem.isPinned              -> UnifiedListItem.isPinned
MessageItem.isBot                 -> UnifiedListItem.badges
MessageItem                       -> UnifiedListItem.detail
```

```text
MailItem.senderName               -> UnifiedListItem.title
MailItem.subject                  -> UnifiedListItem.subtitle
MailItem.preview                  -> UnifiedListItem.preview
MailItem.receivedTime             -> UnifiedListItem.timeText
MailItem.isUnread                 -> UnifiedListItem.isUnread / unreadCount
MailItem.hasAttachment            -> UnifiedListItem.badges
MailItem.mailType                 -> UnifiedListItem.badges / metadata
MailItem                          -> UnifiedListItem.detail
```

Kotlin 侧可以用扩展函数表达：

```kotlin
fun MessageItem.toUnifiedListItem(): UnifiedListItem

fun MailItem.toUnifiedListItem(): UnifiedListItem
```

mapper 的职责是：

- 保留业务模型差异；
- 将字段整理成 UI 可统一消费的结构；
- 让列表、分页和详情页组件不关心数据来自消息还是邮箱；
- 避免 UI 组件直接判断大量业务字段。

### 6.6 SDK 接口

消息和邮箱的 SDK 接口可以先分开设计，因为它们的数据来源和业务含义不同。

推荐接口：

```text
getMessagePage(pageSize, cursor) -> PageResult<MessageItem>
getMailPage(pageSize, cursor) -> PageResult<MailItem>
```

UI 侧通过 mapper 转换后，统一得到：

```text
PageResult<UnifiedListItem>
```

即：

```text
getMessagePage(...) -> PageResult<MessageItem> -> PageResult<UnifiedListItem>
getMailPage(...)    -> PageResult<MailItem>    -> PageResult<UnifiedListItem>
```

## 7. 模块划分建议

建议项目目录逐步演进为：

```text
app/
features/
  message/
    ui/
    data/
    domain/
  mail/
    ui/
    data/
    domain/
shared/
  list/
  ui/
  navigation/
proto/
sdk/
  rust/
openspec/
docs/
  ai-context/
```

职责说明：

- `app`：App 入口、主导航、底部 Tab；
- `features/message`：消息业务；
- `features/mail`：邮箱业务；
- `shared/list`：分页状态、通用列表能力；
- `shared/ui`：通用 UI 组件；
- `shared/navigation`：页面跳转；
- `proto`：UI 与 SDK 共用的数据契约；
- `sdk/rust`：后续 Rust SDK；
- `openspec`：需求、设计、任务和验收证据；
- `docs/ai-context`：AI 可读工程上下文、构建命令、常见错误和修复记录。

## 8. 功能开发流程

本项目强调 AI 原生开发方式。开发过程中应尽量让 AI 完成需求文档、方案设计、代码实现、测试补充和问题修复，人工主要负责提需求、给上下文、审查方案、做取舍、验收结果和记录证据。

目标比例：

```text
AI 完成约 90% 的文档、代码、测试和排障工作；
人工完成约 10% 的需求表达、审查、决策和验收工作。
```

每开发一个功能，原则上按以下流程执行：

1. 人工提出功能目标和约束，例如“实现消息列表分页，复用统一 UI 数据模型”。
2. 创建 OpenSpec change。
3. 让 AI 生成 `proposal.md`、`design.md`、`tasks.md` 初稿。
4. 人工审查 AI 输出，确认需求边界、技术方案和验收标准。
5. 人工补充或修正关键决策，例如接口字段、模块边界、是否接受某个抽象。
6. 让 AI 根据确认后的 `design.md` 和 `tasks.md` 生成或修改代码。
7. 人工阅读 AI 生成的代码 diff，指出问题并让 AI 继续修正。
8. 让 AI 生成或补充测试用例、mock 数据和边界 case。
9. 让 AI 执行或指导执行构建、测试和错误排查。
10. 人工验收运行结果、页面效果、接口行为和文档记录。
11. 让 AI 更新 OpenSpec `tasks.md`，记录完成证据。
12. 记录 AI prompt、AI 输出结论、人工取舍和最终结果。
13. 提交 PR 并进行 review。
14. 功能完成后归档 OpenSpec change。

开发时不要求人工手写大部分代码。人工可以少量修改代码，但更推荐通过清晰 prompt 驱动 AI 修改，例如：

```text
这个实现没有复用 UnifiedListItem，请改成通过 mapper 转换后再渲染。
这个 ViewModel 直接依赖 mock 数据了，请改成依赖 Repository 接口。
这个分页状态缺少 LoadMoreError，请补充状态和测试。
```

人工审查重点：

- AI 是否误解需求；
- 技术方案是否过度设计；
- 是否破坏模块边界；
- 是否引入不存在的依赖；
- 是否符合当前阶段目标；
- 是否复用统一 UI 数据模型；
- 是否复用列表、分页和详情页抽象；
- 是否有可验收的完成标准；
- 构建、测试或人工验收结果是否已经记录。

## 9. AI 使用规范

AI 可以参与：

- 需求拆解；
- 技术方案生成；
- 边界 case 枚举；
- 数据模型草案；
- UI 状态设计；
- 代码骨架生成；
- 测试建议；
- Bazel 构建错误分析；
- 文档整理。

AI 不直接决定：

- 最终技术选型；
- 模块边界；
- 接口契约；
- 是否引入复杂抽象；
- 是否接受某段代码。

每次重要 AI 协作需要记录：

- 使用的 Prompt；
- 输入给 AI 的关键上下文；
- AI 输出的主要建议；
- 人工采用了哪些；
- 人工拒绝了哪些；
- 拒绝原因；
- 最终实现结果。

## 10. Git 协作规范

### 10.1 基本原则

- 不直接在主分支上开发功能；
- 每个功能或修复创建独立分支；
- 提交前先查看改动范围；
- 提交信息要能说明本次变更目的；
- PR 需要关联对应 OpenSpec change；
- 合并前至少由另一位成员 review。

### 10.2 常用命令

查看当前状态：

```bash
git status
```

创建并切换功能分支：

```bash
git checkout -b feature/message-list
```

查看改动：

```bash
git diff
```

添加文件：

```bash
git add <file>
```

提交：

```bash
git commit -m "feat: add message list ui"
```

拉取远端更新并变基：

```bash
git pull --rebase
```

推送当前分支：

```bash
git push -u origin feature/message-list
```

查看提交历史：

```bash
git log --oneline
```

查看分支：

```bash
git branch
```

切换分支：

```bash
git checkout <branch-name>
```

### 10.3 分支命名

建议使用：

```text
feature/message-list
feature/mail-card-list
feature/sdk-contract
feature/proto-models
fix/message-pagination
docs/openspec-init
build/bazel-message-target
```

### 10.4 Commit 信息

建议格式：

```text
type: summary
```

常用类型：

- `feat`：新增功能；
- `fix`：修复问题；
- `docs`：文档更新；
- `refactor`：重构；
- `test`：测试；
- `build`：构建相关；
- `chore`：杂项维护。

示例：

```text
feat: add message list screen
feat: define sdk page result contract
docs: add openspec change for mail cards
build: add bazel target for proto models
```

### 10.5 PR 描述要求

PR 描述至少包含：

- 本次改动内容；
- 关联的 OpenSpec change；
- AI 参与点；
- 人工取舍说明；
- 测试或构建结果；
- 是否影响接口契约。

## 11. 文档沉淀规范

项目中需要持续沉淀以下文档：

- OpenSpec `proposal.md`；
- OpenSpec `design.md`；
- OpenSpec `tasks.md`；
- delta specs；
- AI prompt 记录；
- 接口契约变更记录；
- 构建命令记录；
- 常见错误和修复方式；
- 阶段复盘文档。

建议后续新增：

```text
docs/project/project-structure.md
docs/project/module-boundaries.md
docs/ai-context/build-system/build-commands.md
docs/ai-context/build-system/common-build-errors.md
docs/ai-context/ai-prompts.md
```

## 12. 第一阶段落地任务

第一阶段目标：跑通消息和邮箱两个页面的 UI 主链路，并稳定数据契约。

建议任务：

1. 初始化项目目录和基础 Android App。
2. 确认 `MessageItem`、`MailItem`、`PageResult`、`UnifiedListItem`、`DetailModel` 字段。
3. 实现 Bottom Tab：消息 / 邮箱。
4. 实现消息列表 mock 数据和分页加载。
5. 实现消息详情页。
6. 实现邮箱卡片列表 mock 数据和分页加载。
7. 实现邮件详情页。
8. 抽取通用分页 UI 状态。
9. 实现 `MessageItem -> UnifiedListItem` 映射。
10. 实现 `MailItem -> UnifiedListItem` 映射。
11. 使用统一 UI 模型复用列表和详情页抽象。
12. 定义 proto 初稿。
13. 定义 SDK mock 接口。
14. 补充 OpenSpec change 和 AI 使用记录。

第一阶段不强制完成：

- 完整 Rust SDK；
- 完整 Bazel 迁移；
- Trae 插件；
- 复杂缓存；
- 真实后端服务。

这些内容进入后续阶段逐步实现。

## 13. 当前已确认决策

- 采用类前后端分离协作方式；
- 前端侧负责 Android UI，后端侧负责 SDK、proto 和构建；
- 技术路线采用“契约先行，UI 与 SDK mock 并行”；
- 消息列表采用飞书风格高密度会话列表；
- 邮箱列表采用 QQ 邮箱提醒风格卡片列表；
- SDK 接口先拆分为消息接口和邮箱接口；
- SDK / proto 层保留 `MessageItem` 和 `MailItem` 两类业务模型；
- UI 层统一使用 `UnifiedListItem` 和 `DetailModel`；
- 通过 mapper 将 `MessageItem`、`MailItem` 转换为统一 UI 模型；
- 列表、分页状态和详情页抽象必须复用；
- 分页结果结构复用；
- 第一阶段先跑通 UI 和 mock 数据，不让 Rust/Bazel 阻塞主链路。
