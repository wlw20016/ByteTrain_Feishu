# 邮箱功能模块

邮箱 Tab 功能模块。

职责：

- 渲染邮件列表。
- 提供邮件详情页跳转入口。
- 维护邮箱功能自己的 UI 状态和数据映射。
- 复用 shared 中的分页和列表抽象。

规划中的 Bazel targets：

```text
//features/mail:mail_ui
//features/mail:mail_domain
//features/mail:mail_data
```
