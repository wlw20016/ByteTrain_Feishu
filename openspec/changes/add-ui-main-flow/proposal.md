# Proposal: add-ui-main-flow

## 背景

当前仓库只有 Android 入口占位类和领域模型骨架。课题要求交付一个可运行的 Android App，包含消息和邮箱两个 Tab，每个 Tab 支持 10000 条 mock 数据、分页加载、二级详情页、可复用列表/详情抽象，以及清晰的 UI 状态设计。

## 变更内容

- 实现真实 Android App 入口和双 Tab 导航。
- 新增临时 Android+Gradle 构建入口，用于第一阶段 UI 编译和运行验证。
- 新增统一 UI 列表模型，并完善分页状态模型。
- 实现消息列表、消息详情、邮箱列表、邮箱详情主链路。
- 新增 10000 条 mock 数据仓库，并支持基于 `pageSize/cursor` 的分页。
- 消息和邮箱复用共享列表、分页和详情抽象。
- 补充 UI 状态矩阵、mapper 测试、repository 测试和人工验收记录。

## 影响范围

- 涉及 `app/`、`features/message/`、`features/mail/`、`shared/list`、`shared/ui` 和 `shared/navigation`。
- 第一阶段通过 Gradle 先验证 Android UI 可编译运行；完整 Bazel 接入仍由 `wire-bazel-build` 跟踪。
- 本 change 是项目第一阶段可视化功能验收的核心里程碑。
