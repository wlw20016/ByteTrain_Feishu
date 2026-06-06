这个 md 文档的核心不是单纯“做一个仿飞书 App”，而是要你们完成一次 **AI + Bazel + OpenSpec 驱动的 Android 原生工程实践**。也就是说，功能只是载体，真正考察的是：需求怎么拆、工程怎么组织、构建怎么管理、AI 怎么参与、文档证据怎么沉淀。

---

# 一、这个项目要用到哪些技术？

## 1. Android 原生开发：Kotlin

你们最终要做的是一个 **仿飞书 Android App**，包含两个核心 Tab：

* 消息 Tab
* 邮箱 Tab

文档要求 App 层使用 **Kotlin**，并且消息页和邮箱页都要展示 **10000 条数据**，支持分页、列表滚动、点击跳转二级详情页。

你可以理解为：

```text
Kotlin = 写 Android 页面和业务逻辑的主要语言
```

前端同学可以类比：

```text
Kotlin Android ≈ React / Vue 写页面
Activity / Fragment / Compose 页面 ≈ 页面组件
RecyclerView / LazyColumn ≈ 长列表组件
ViewModel ≈ 状态管理层
```

这个项目中，Android 主要负责：

```text
页面展示
Tab 切换
列表渲染
分页加载
点击跳转
加载态 / 空态 / 错误态
调用 SDK 获取数据
```

---

## 2. UI 技术：Jetpack Compose 或传统 Android View

文档里允许两种 UI 实现方式：

```text
Jetpack Compose
传统 Android View / RecyclerView
```

对于你这种有 Web 前端基础、第一次接触 Android 的情况，我更建议使用 **Jetpack Compose**。

因为 Compose 的思想更接近前端组件化：

```kotlin
@Composable
fun MessageListScreen() {
    LazyColumn {
        items(messages) { message ->
            MessageItem(message)
        }
    }
}
```

可以类比成：

```tsx
function MessageListScreen() {
  return messages.map(message => <MessageItem message={message} />)
}
```

如果用传统 Android View，则会涉及：

```text
XML 布局
Activity / Fragment
RecyclerView
Adapter
ViewHolder
```

这套东西更偏传统 Android，学习成本会更高。

所以推荐技术路线是：

```text
Kotlin + Jetpack Compose + ViewModel + StateFlow
```

---

## 3. Rust SDK

文档要求底层服务使用 **Rust SDK**，负责：

```text
数据解析
异步读取
服务接口
protobuf 通信
```

也就是说，Android UI 不应该直接自己造数据、解析数据，而是要通过一个 SDK 层拿数据。

整体关系大概是：

```text
Android UI
   ↓
Kotlin 调用 SDK 接口
   ↓
Rust SDK
   ↓
mock 数据 / 解析逻辑 / 异步读取
```

你可以把 Rust SDK 类比成 Web 项目里的：

```text
service 层
api 层
数据适配层
```

区别是它不是用 TypeScript 写，而是用 Rust 写，并且将来可能通过 JNI / FFI / UniFFI 等方式暴露给 Kotlin 使用。

---

## 4. Protobuf

文档要求 SDK 和 UI 使用 **protobuf** 通信。

protobuf 的作用是定义稳定的数据协议，例如：

```proto
message MessageItem {
  string id = 1;
  string title = 2;
  string content = 3;
  int64 timestamp = 4;
  bool unread = 5;
}
```

你可以理解为：

```text
protobuf ≈ 更严格、更高性能、更跨语言的 TypeScript interface
```

比如前端里你可能会写：

```ts
interface MessageItem {
  id: string
  title: string
  content: string
  timestamp: number
  unread: boolean
}
```

但这个项目里，Kotlin 和 Rust 都要使用同一份数据结构，所以用 protobuf 来做统一协议。

---

## 5. Bazel 构建系统

Bazel 是这个项目的工程化重点。

文档要求用 Bazel 管理：

```text
Android App
Kotlin 模块
Rust SDK
proto 文件
测试
工具链依赖
构建产物
```

并且需要支持：

```bash
bazel build
bazel run
bazel test
bazel query
bazel clean
```

你可以先粗略类比：

```text
Bazel ≈ 更大型、更严格、更可复现的 Vite / Webpack / Gradle / Turborepo
```

但 Bazel 不只是打包工具，它更像一个完整的工程构建系统。

在这个项目里，Bazel 主要负责：

```text
模块边界
依赖关系
构建流程
测试流程
多语言协同
构建缓存
依赖分析
```

比如你会为不同模块写 BUILD 文件：

```text
app/BUILD.bazel
features/message/BUILD.bazel
features/mail/BUILD.bazel
sdk/rust/BUILD.bazel
proto/BUILD.bazel
```

这样 Bazel 才知道：

```text
谁依赖谁
谁可以被谁调用
哪个模块怎么构建
哪个 target 怎么测试
```

---

## 6. OpenSpec

OpenSpec 是这个项目的流程管理重点。

文档要求所有重要变更都要先写 OpenSpec change，再实现和验收。

也就是说，不能一上来就写代码，而是要先写：

```text
proposal.md  这个功能为什么要做
design.md    准备怎么做，有哪些技术方案和取舍
tasks.md     具体拆成哪些任务
delta specs  这个功能对系统能力有什么增量变化
```

你可以把 OpenSpec 理解成：

```text
需求文档 + 技术设计 + TODO 清单 + 验收标准 + 变更记录
```

它的作用是让团队开发有证据链：

```text
需求从哪里来
为什么这么设计
AI 给了什么建议
人做了什么取舍
代码实现了什么
最后怎么验收
```

---

## 7. AI IDE：Trae / VS Code

文档要求支持 Trae / VS Code IDE 编译、运行、索引、断点和代码提示，并且要产出 VS Code 插件或类似的构建辅助能力。

这里的重点不是“装一个 IDE 就行”，而是要让 AI 能读懂项目。

所以你们需要沉淀：

```text
项目目录说明
模块边界说明
常用 Bazel 命令
构建失败日志
修复记录
AI 可读取的上下文文档
```

例如：

```text
docs/project/project-structure.md
docs/ai-context/build-system/build-commands.md
docs/ai-context/build-system/common-build-errors.md
docs/project/module-boundaries.md
```

这样以后你问 AI：

```text
为什么这个 Kotlin 模块引用不到 proto 生成代码？
```

AI 才能根据 BUILD 文件、query 结果和错误日志帮你定位问题。

---

# 二、推荐的整体技术架构

我建议你把项目理解成下面这个结构：

```text
feishu-clone-android/
├── app/                         # Android App 入口
│   ├── MainActivity.kt
│   └── BUILD.bazel
│
├── features/
│   ├── message/                 # 消息 Tab
│   │   ├── ui/
│   │   ├── data/
│   │   ├── domain/
│   │   └── BUILD.bazel
│   │
│   └── mail/                    # 邮箱 Tab
│       ├── ui/
│       ├── data/
│       ├── domain/
│       └── BUILD.bazel
│
├── shared/
│   ├── ui/                      # 通用 UI 组件
│   ├── list/                    # 通用分页列表抽象
│   ├── navigation/              # 页面跳转
│   └── BUILD.bazel
│
├── proto/
│   ├── message.proto
│   ├── mail.proto
│   └── BUILD.bazel
│
├── sdk/
│   └── rust/                    # Rust SDK
│       ├── src/
│       ├── Cargo.toml
│       └── BUILD.bazel
│
├── openspec/
│   ├── project.md
│   ├── specs/
│   └── changes/
│
├── docs/
│   └── ai-context/
│
├── MODULE.bazel
├── BUILD.bazel
└── README.md
```

每一层的职责是：

```text
app：负责启动 App 和挂载主页面
features/message：消息功能
features/mail：邮箱功能
shared：消息和邮箱共用的列表、状态、UI 组件
proto：Kotlin 和 Rust 共用的数据协议
sdk/rust：底层数据解析和异步接口
openspec：需求、设计、任务、验收证据
docs/ai-context：给 AI 看的工程说明和排障知识库
```

---

# 三、每次开发一个功能时，应该怎么做？

后续每开发一个功能，都不要直接写代码，而是按照固定流程走。

建议你们团队统一使用这个流程：

```text
1. 开 OpenSpec change
2. 拆需求和验收标准
3. 设计模块边界
4. 让 AI 生成方案和代码骨架
5. 人工审查和取舍
6. 写代码
7. 写测试
8. Bazel build/test
9. 记录构建和测试结果
10. 更新 OpenSpec tasks
11. PR review
12. archive 归档
```

下面我按真实开发流程讲。

---

## 第 1 步：先建立 OpenSpec change

比如你要开发“消息列表分页功能”，不要直接写代码。

先创建：

```text
openspec/changes/add-message-pagination/
├── proposal.md
├── design.md
├── tasks.md
└── specs/
    └── message/spec.md
```

### proposal.md 写什么？

写清楚这个功能为什么要做。

例如：

```md
# Proposal: add-message-pagination

## Why

消息 Tab 需要展示 10000 条消息数据，如果一次性渲染全部数据，会造成首屏加载慢、内存占用高和滚动卡顿。因此需要支持分页加载。

## What

- 消息列表首屏只加载一页数据
- 根据屏幕可展示数量决定分页大小
- 支持上拉加载更多
- 支持加载态、错误态、空态
- 支持点击进入消息详情页

## Impact

- 新增 MessageRepository 分页接口
- 新增 MessageListUiState
- 新增分页列表组件
- 影响 message feature 和 shared list 模块
```

---

## 第 2 步：写 design.md

design.md 写具体技术方案。

例如：

```md
# Design

## UI

使用 Jetpack Compose 的 LazyColumn 实现长列表。

## State

定义 MessageListUiState：

- Loading
- Empty
- Error
- Content
- LoadingMore
- LoadMoreError

## Data

MessageRepository 提供：

- loadInitial()
- loadMore(cursor)

## Performance

- 不一次性渲染 10000 条数据
- 使用 key 保持 item 稳定性
- 避免在 item 中做重计算
```

这里可以让 AI 参与生成：

```text
请基于 Android Jetpack Compose 长列表场景，帮我设计消息列表 10000 条数据分页加载方案，包括 UI 状态、错误状态、性能风险和测试点。
```

但是 AI 的输出不能直接无脑用。你要在 design.md 里写清楚：

```text
AI 建议了哪些方案
我们采用了哪些
拒绝了哪些
为什么
```

---

## 第 3 步：写 tasks.md

tasks.md 是真正的开发清单。

例如：

```md
# Tasks

## 1. Data model

- [ ] 定义 MessageItem proto
- [ ] 生成 Kotlin / Rust 数据结构
- [ ] 定义 MessageRepository 接口

## 2. UI

- [ ] 实现 MessageListScreen
- [ ] 实现 MessageItemCell
- [ ] 实现 Loading / Empty / Error 状态
- [ ] 实现上拉加载更多

## 3. Navigation

- [ ] 点击消息进入 MessageDetailScreen
- [ ] 详情页展示标题、内容、时间

## 4. Test

- [ ] 添加 Repository 单测
- [ ] 添加 UI 状态测试
- [ ] 添加分页边界测试

## 5. Build

- [ ] 新增 message/BUILD.bazel
- [ ] bazel build 通过
- [ ] bazel test 通过
```

你每完成一个任务，就把勾打上，并补证据：

```md
- [x] 实现 MessageListScreen
  - Evidence: PR #12
  - Build: bazel build //features/message:message_ui passed
```

---

## 第 4 步：设计模块边界

写代码前，先确定这个功能涉及哪些模块。

比如消息列表分页会涉及：

```text
features/message/ui
features/message/domain
features/message/data
shared/list
proto
sdk/rust
```

要避免所有代码都堆在一个 MainActivity 里。

推荐结构：

```text
features/message/
├── ui/
│   ├── MessageListScreen.kt
│   ├── MessageItemCell.kt
│   └── MessageDetailScreen.kt
│
├── domain/
│   ├── MessageItem.kt
│   └── MessageRepository.kt
│
├── data/
│   ├── MockMessageRepository.kt
│   └── SdkMessageRepository.kt
│
└── MessageViewModel.kt
```

这样你写邮箱功能时，就可以复用同一套结构。

---

## 第 5 步：让 AI 生成代码骨架，但不要直接全盘接受

比如你可以问 AI：

```text
请基于 Kotlin + Jetpack Compose，生成 MessageListScreen、MessageItemCell、MessageViewModel、MessageRepository 的代码骨架。要求支持 Loading、Empty、Error、Content、LoadingMore 状态。
```

AI 可以帮你快速生成初稿。

但你需要人工检查：

```text
状态设计是否合理
是否和项目目录一致
是否引入了不存在的依赖
是否破坏了模块边界
是否适合 Bazel 构建
是否有过度设计
```

这个项目特别强调：

```text
AI 参与，但人工负责最终判断
```

文档也明确要求每个阶段要留下 Prompt、关键上下文、AI 输出结论、人工取舍和最终结果。

---

## 第 6 步：实现功能

实现时建议顺序是：

```text
先 mock 数据
再 UI 展示
再分页状态
再详情跳转
再接 SDK
最后接 Bazel
```

不要一开始就同时搞：

```text
Android + Rust + protobuf + Bazel + AI IDE
```

这样很容易崩。

对于第一次做 Android 和团队协作开发，我建议采用渐进路线：

```text
阶段 1：Gradle + Android Studio 跑通 UI
阶段 2：抽象消息和邮箱的共用列表模型
阶段 3：接入 mock SDK
阶段 4：接入 protobuf
阶段 5：接入 Rust SDK
阶段 6：迁移到 Bazel
阶段 7：沉淀 AI 构建 skill 和排障文档
```

文档里也明确说基础 UI 阶段可以先使用 **Gradle + Android Studio**，后续再从 Gradle + Android Studio 迁移到 Bazel + VS Code / Trae。

---

# 四、每个功能的开发模板

以后你们开发任何一个功能，都可以套这个模板。

## 功能开发标准流程

```text
功能名：消息列表分页

1. OpenSpec
   - 创建 openspec/changes/add-message-pagination
   - 写 proposal.md
   - 写 design.md
   - 写 tasks.md
   - 写 delta spec

2. 需求确认
   - 这个功能解决什么问题？
   - 用户怎么使用？
   - 有哪些状态？
   - 有哪些边界情况？
   - 怎么验收？

3. 技术设计
   - 涉及哪些模块？
   - 数据模型是什么？
   - UI 状态怎么设计？
   - 是否要复用 shared 模块？
   - 是否涉及 proto / SDK / Bazel？

4. AI 协作
   - 让 AI 生成方案
   - 让 AI 枚举边界 case
   - 让 AI 生成代码骨架
   - 让 AI 生成测试建议
   - 记录 Prompt 和人工取舍

5. 编码实现
   - 先写 model
   - 再写 repository
   - 再写 ViewModel
   - 再写 UI
   - 再写 navigation
   - 再接 SDK

6. 测试
   - 单测
   - UI 状态测试
   - 分页边界测试
   - 构建测试

7. Bazel 验证
   - bazel build
   - bazel test
   - bazel query

8. 文档更新
   - 更新 tasks.md
   - 写构建记录
   - 写 AI 使用记录
   - 写问题和修复记录

9. PR
   - 关联 OpenSpec change
   - 说明实现内容
   - 说明测试结果
   - 说明 AI 参与点

10. Archive
   - 功能完成后归档 spec
   - 沉淀为长期项目规范
```

---

# 五、具体功能应该怎么拆？

## 1. 消息 Tab

消息 Tab 是第一个核心功能。

建议拆成：

```text
消息数据模型
消息列表 UI
消息 Item 组件
消息分页加载
消息详情页
消息状态管理
消息 mock 数据
消息 Bazel target
消息测试
```

开发顺序：

```text
1. 定义 MessageItem
2. 写 10000 条 mock 数据
3. 写 MessageListScreen
4. 写 MessageItemCell
5. 加 LazyColumn / RecyclerView
6. 加分页
7. 加 Loading / Empty / Error
8. 加点击跳转详情页
9. 写测试
10. 接 Bazel
```

---

## 2. 邮箱 Tab

邮箱 Tab 不应该完全重新写一遍，而是要复用消息页的抽象。

文档明确要求邮箱页复用消息页的列表分页、数据状态和二级页跳转抽象。

所以开发邮箱前，要先抽出：

```text
通用列表状态
通用分页模型
通用详情跳转模式
通用 Cell 布局结构
```

例如：

```kotlin
sealed interface PagingUiState<T> {
    data object Loading : PagingUiState<Nothing>
    data object Empty : PagingUiState<Nothing>
    data class Error(val message: String) : PagingUiState<Nothing>
    data class Content<T>(
        val items: List<T>,
        val hasMore: Boolean,
        val isLoadingMore: Boolean
    ) : PagingUiState<T>
}
```

然后消息和邮箱分别使用：

```kotlin
PagingUiState<MessageItem>
PagingUiState<MailItem>
```

这样才符合工程化要求。

---

## 3. Rust SDK

Rust SDK 不建议一开始就做得很复杂。

可以分三步：

```text
第一步：Rust 内部生成 mock 数据
第二步：Rust 提供异步接口
第三步：通过 protobuf 和 Kotlin 通信
```

Rust SDK 应该负责：

```text
生成 / 读取消息数据
生成 / 读取邮件数据
解析 protobuf
返回分页结果
处理错误
提供测试
```

需要重点设计：

```text
错误模型
异步接口
分页参数
返回数据结构
mock 数据生成方式
protobuf 契约
```

---

## 4. Protobuf

proto 可以先定义这些：

```text
MessageItem
MailItem
PageRequest
PageResponse
SdkError
```

例如：

```proto
message PageRequest {
  int32 page_size = 1;
  string cursor = 2;
}

message PageResponse {
  repeated MessageItem items = 1;
  string next_cursor = 2;
  bool has_more = 3;
}
```

注意：消息和邮箱可以共用分页协议，但具体 item 不一样。

---

## 5. Bazel

Bazel 不建议最后一天才接，因为很容易出现大量依赖问题。

推荐从小 target 开始：

```text
先让 proto target 能 build
再让 shared target 能 build
再让 message target 能 build
再让 mail target 能 build
再让 app target 能 build
最后接 rust sdk
```

每个模块都应该有自己的 BUILD 文件。

比如：

```text
//features/message:message_ui
//features/message:message_domain
//features/mail:mail_ui
//shared/list:paging
//proto:message_proto
//sdk/rust:rust_sdk
//app:app
```

并且每次遇到 Bazel 报错，都要沉淀到：

```text
docs/ai-context/build-system/common-build-errors.md
```

---




# 六、最重要的开发原则

这个项目后续每做一个功能，你都要记住一句话：

```text
先写清楚为什么做、怎么做、怎么验收，再写代码。
```

对于这个 md 文档来说，最终验收不会只看 App 能不能跑，而是会看：

```text
有没有 OpenSpec 文档
有没有 Bazel target
有没有构建记录
有没有测试记录
有没有 AI 使用记录
有没有人工取舍说明
有没有模块边界
有没有复用抽象
有没有沉淀排障经验
```

所以你不能把它当成普通 Android Demo。

更准确地说，它是一个：

```text
Android App 功能 Demo
+ Rust SDK
+ protobuf 协议
+ Bazel 工程化
+ OpenSpec 需求流程
+ AI 协作证据链
+ IDE 构建协同
```

你后续每开发一个功能，都按这个闭环来做：

```text
OpenSpec 立项
→ AI 辅助方案
→ 人工确认设计
→ 编码实现
→ Bazel 构建
→ 测试验证
→ 文档记录
→ PR review
→ archive 归档
```

这就是这个文档真正想训练你们的能力。
