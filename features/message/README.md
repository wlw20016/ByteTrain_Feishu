# 消息功能模块

消息 Tab 功能模块。

职责：

- 渲染消息列表。
- 提供消息详情页跳转入口。
- 维护消息功能自己的 UI 状态和数据映射。
- 复用 shared 中的分页和列表抽象。

规划中的 Bazel targets：

```text
//features/message:message_ui
//features/message:message_domain
//features/message:message_data
```
