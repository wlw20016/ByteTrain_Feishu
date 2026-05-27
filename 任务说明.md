# Android AI + Bazel + OpenSpec 原生移动端工程课题：仿飞书 App 实战

## 核心定位

这不是“用 AI 写几段代码”的练习，而是一次围绕 **Bazel 工程化底座** 和 **OpenSpec 规范流程**，让 AI 参与需求拆解、代码实现、构建排障、质量验证和知识沉淀的端到端移动端工程实践。

## 课题名称

开发一个仿飞书 Android App，包含 **消息** 和 **邮箱** 两个 Tab。

课题重点从“功能实现”升级为 **“AI + Bazel + OpenSpec 驱动的大型移动端工程交付”**：既要能跑起来，也要把需求、设计、任务、实现、验收和变更归档在仓库中。

---

## 课题要求

功能上保持简洁，但工程形态贴近商业级大规模 App。

交付过程中要求主动使用 AI 完成：

- 需求澄清
- 方案生成
- 代码骨架
- 单测建议
- 构建问题定位
- 文档沉淀

同时要求：

- 使用 Bazel 建立稳定、可复现、可分析的工程底座；
- 使用 OpenSpec 将各阶段文档放入仓库，形成可 review、可追踪、可归档的证据链。

## 目标维度

| 目标维度 | 基础要求 | AI + Bazel 增强要求 |
| --- | --- | --- |
| 移动端体验 | 消息和邮箱双 Tab，列表 10000 条数据，支持二级页跳转，使用 Kotlin 语言 | 用 AI 生成 UI 状态矩阵、边界 case 和性能优化建议 |
| 底层服务 | Rust SDK 负责数据解析、异步读取和 protobuf 通信 | 让 AI 生成协议草案、错误模型、异步接口示例和测试样例 |
| Bazel 工程化 | 使用 Bazel 管理模块、依赖、构建、测试和产物，可以运行 `bazel build`、`bazel run`、`bazel test` | 让 AI 基于 BUILD 文件、依赖图、query 结果和失败日志进行诊断 |
| OpenSpec 流程 | 所有重要变更先写 spec/change，再实现和验收 | 用 proposal、design、tasks、delta specs 记录 AI 参与和人工决策 |
| AI IDE 协作 | 支持 Trae / VS Code IDE 编译、运行、索引、断点和代码提示，产出 VS Code 插件 | 将构建命令、日志、模块边界、常见修复路径整理成 AI 可消费上下文 |

## AI 使用原则

每个阶段都要留下可复用的：

- Prompt
- 关键上下文
- AI 输出结论
- 人工取舍
- 最终结果

这些证据必须进入仓库中的 OpenSpec change 文档或关联 PR，而不是散落在聊天记录里。

---

## 最终技术架构

### App 与 SDK 层

- 消息页、邮箱页使用 Kotlin。
- 复用同一套列表、分页和详情页抽象。
- Rust SDK 提供数据解析、异步读取和服务接口。
- protobuf 定义 UI 与 SDK 的稳定通信契约。

### Bazel 与 AI 工程底座

- Bazel 管理 Kotlin、Rust、proto、测试和工具链依赖。
- VSCode或Trae IDE 负责 AI 编码、索引、断点和构建反馈。
- AI 读取 query、日志和模块说明，辅助定位工程问题。
- 需要沉淀构建相关 skill，帮助 AI 理解 build 流程。

> 暂时无法在飞书文档外展示此内容。

通过这个课题，希望你能了解：

- 基础 Kotlin UI 开发
- Rust 移动端 SDK
- protobuf 契约设计
- Bazel 构建系统
- OpenSpec 变更管理
- IDE 与构建系统协同
- AI 如何在大型工程中获得可衡量的实际价值

---

## 任务拆解

## AI 驱动的需求与方案设计

### 【P0】OpenSpec 初始化与任务建模【挑战：⭐️】

- 在仓库中初始化 OpenSpec 目录，建立项目说明、现有能力 spec、变更 change 目录。
- 将“仿飞书消息/邮箱”拆成页面、数据、交互、性能、构建、测试六类需求。
- 每个阶段性变更都先提交 OpenSpec change：
  - `proposal.md`
  - `design.md`
  - `tasks.md`
  - delta specs
- 使用 AI 生成初稿，再由人工确认优先级、边界、验收标准和技术取舍。
- 沉淀 Prompt：需求拆解、风险识别、接口草案、Bazel 目标规划、测试用例生成，并写入对应 change 或 PR 描述。
- 每个 P0 任务都必须有对应 OpenSpec change，并在 `tasks.md` 中标记完成证据。

## 基础 UI 开发

在这个阶段，可以使用 **Gradle + Android Studio**。

### 【P0】消息 Tab 开发【挑战：⭐️】

- 从零搭建 Android App 开发环境。
- 展示 10000 条消息数据。
- Cell / Item 布局使用 Android 原生 UI API，支持不同屏幕尺寸适配。
- 可使用 Jetpack Compose，也可以使用传统 Android View / RecyclerView。
- 按屏幕可展示数量分页，支持上拉加载更多。
- 模拟飞书 Feed 页面，支持点击跳转二级页面。
- 使用 AI 生成 UI 状态枚举、空态、加载态、错误态方案和性能排查清单。

### 【P0】邮箱 Tab 开发【挑战：⭐️】

- 展示 10000 条邮件数据。
- 复用消息页的列表分页、数据状态和二级页跳转抽象。
- 使用 AI 对比消息与邮箱的信息结构，输出可复用 UI 数据模型。

## SDK 与协议层

### 【P0】Rust SDK 开发【挑战：⭐️⭐️】

- 使用 Rust 开发 SDK，解析数据并返回给上层 UI。
- UI 数据读取从同步切换成异步。
- 定义 proto，SDK 和 UI 使用 protobuf 通信。
- 使用 AI 生成 Rust 错误处理策略、异步 API 示例、mock 数据和单测样例。

## Bazel 工程化主线

从 **Gradle + Android Studio** -> **Bazel + VS Code / Trae**。

### 【P0】Bazel 基础构建接入【挑战：⭐️⭐️⭐️】

- 为 App、UI 模块、SDK、proto 和测试建立清晰的 Bazel target。
- 使用 BUILD 文件表达模块边界、依赖关系和可见性约束。
- 建立常用命令：
  - `bazel build`
  - `bazel run`
  - `bazel test`
  - `bazel query`
  - `bazel clean`
- 用 AI 检查 BUILD 文件是否存在依赖穿透、target 粒度过粗或可见性过宽。

### 【P1】Bazel 可观测性与构建排障【挑战：⭐️⭐️⭐️⭐️】

- 使用 `bazel query` / `cquery` / `aquery` 分析依赖、配置和 action。
- 记录构建耗时、缓存命中率、失败类型和修复耗时。
- 让 AI 基于日志和 query 结果给出根因，再由人工验证并沉淀结论。

## OpenSpec 阶段文档要求

### 【P0】仓库内文档化交付【挑战：⭐️⭐️】

所有阶段文档必须随代码一起提交到仓库。导师验收时优先看 OpenSpec 文档与代码 diff 是否一致，而不是看单独汇报 PPT。

| 阶段 | 仓库内必须有的文档 | AI 记录方式 | 导师检查点 |
| --- | --- | --- | --- |
| 立项 | `openspec/project.md`、初始 capability spec | AI 生成需求拆解初稿，学生修正范围和验收口径 | 目标是否清晰，是否能映射到后续 tasks |
| 方案 | `proposal.md`、`design.md` | AI 给出方案备选、风险和取舍，学生写明采用/拒绝原因 | 是否先设计再实现，是否能解释 Bazel 边界 |
| 执行 | `tasks.md`、delta specs、关联 PR | 每个任务记录 AI 帮助点、人工修改点、最终 evidence | tasks 是否逐项关闭，代码是否兑现 spec |
| 验收 | 测试记录、构建记录、OpenSpec validate 结果 | AI 参与测试补全和构建排障，但必须附人工验证 | Bazel build/test 是否通过，失败是否有闭环 |
| 归档 | archive 后的 spec、复盘文档 | 记录哪些 AI 模板可复用，哪些建议无效 | 变更是否从 proposal 流转成长期规范 |

## AI 原生工程能力

### 【P0】AI 可读工程上下文建设【挑战：⭐️⭐️】

- 整理目录结构、模块边界、接口契约和常见命令，形成 AI 可直接读取的工程说明。
- 为每个模块补充 README 或注释入口，说明“该让 AI 看哪里、不要改哪里”。
- 将构建失败日志、关键报错、修复记录沉淀成排障知识库。

### 【P1】Trae IDE 深度接入【挑战：⭐️⭐️⭐️】

- 支持 Trae IDE 编译和运行。
- 支持断点、索引、代码提示和跨 Kotlin、Rust、proto、BUILD 文件的问题定位。
- 验证 AI 能否基于索引和 Bazel 结构完成跨模块问题定位。

---

## 建议人力分工

建议组长用飞书多维表格跟踪进度，但正式过程证据以仓库中的 OpenSpec 文档为准。

表格只记录入口链接，例如：

- change id
- PR
- Bazel 构建记录
- 复盘链接

| 时间 | 主线任务 | 建议人力 | OpenSpec / Bazel 产出物 |
| --- | --- | --- | --- |
| 11.22 | OpenSpec 初始化、需求拆解、验收标准 |  | `project.md`、初始 specs、首个 `proposal/design/tasks` |
| 11.23 ~ 11.30 | 基础 Android、邮箱页、SDK 并行开发 |  | OpenSpec changes、PR、接口草案、状态矩阵、单测样例 |
| 12.01 ~ 12.07 | Bazel、Trae IDE、质量闭环 |  | BUILD 文件、依赖图、query 结果、validate/archive、复盘文档 |

---

## 额外补充要求

以下内容来自细节说明，需要体现在后续任务或 OpenSpec change 中：

- 支持 Trae，做一个插件来支持自动的构建。
- 数据的异步获取，数据可以完全 mock。
- 构建的内容是对 AI 友好的，写一个构建过程的 skill。
- 说明 IDE 是如何和 Bazel 做协同的。
