# App 模块

Android 应用入口模块。

职责：

- 承载主 Activity。
- 挂载消息和邮箱两个 Tab。
- 组织应用级导航。
- 通过稳定边界依赖功能模块。

规划中的 Bazel target：

```text
//app:app
```
