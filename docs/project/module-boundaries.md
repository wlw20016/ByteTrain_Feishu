# 模块边界

## App

App 模块只负责应用启动和应用级导航。

Bazel target：

- `//app:app`
- `//app:app_lib`

`//app:app` 只直接依赖 `//app:app_lib`。`//app:app_lib` 组合 feature 与 shared target：

- `//features/message:domain`
- `//features/message:data`
- `//features/message:mapper`
- `//features/message:ui`
- `//features/mail:domain`
- `//features/mail:data`
- `//features/mail:mapper`
- `//features/mail:ui`
- `//shared/navigation:navigation`
- `//shared/ui:ui_android`
- `//shared/ui:ui_models`

BZL-FINAL query 证据：

```powershell
bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"
```

结果摘要：2026-06-05 通过。输出包含 `android_binary rule //app:app`、`kt_android_library rule //app:app_lib`，以及上方列出的 feature/shared 显式依赖。query 同时显示 app manifest、`AppRepositoryProvider.kt`、`MainActivity.kt` 和两个 app drawable 资源文件属于 app target 边界。最终 query 未引入新的跨层依赖，只补充了 app 内部 provider 源文件。

## 功能模块

功能模块负责单个产品区域内的领域模型、功能状态、数据适配和 UI。

Bazel target：

- Message：`//features/message:domain`、`//features/message:data`、`//features/message:mapper`、`//features/message:ui`、`//features/message:message`
- Mail：`//features/mail:domain`、`//features/mail:data`、`//features/mail:mapper`、`//features/mail:ui`、`//features/mail:mail`

Feature target 允许依赖自身 domain/data/mapper/ui 分层、shared list state、shared UI model 和 shared Android UI abstraction。Feature 不依赖 `//app:*`。

## Shared

Shared 模块不能依赖具体 feature 模块。

Bazel target：

- `//shared/list:list`
- `//shared/navigation:navigation`
- `//shared/ui:ui_android`
- `//shared/ui:ui_models`

Shared target 提供跨 feature 复用模型、Android View UI primitives、分页状态和导航常量，不依赖 app 或具体 feature。`//shared/ui:ui_android` owns the shared list shell, load-more footer, badge row, scroll restoration, dp/color helpers, and detail header/back affordance used by message and mail UI.

## Proto

Proto 文件定义稳定的跨语言数据契约。

Bazel target：

- `//proto:paging_proto`
- `//proto:mail_proto`
- `//proto:message_proto`
- `//proto:feed_proto`
- `//proto:...` alias，兼容 BZL-002 指定命令

## Rust SDK

Rust SDK 负责数据解析、异步服务边界，以及后续面向 FFI 的逻辑。

Bazel target：

- `//sdk/rust:bytetrain_feed_sdk`
- `//sdk/rust:bytetrain_feed_sdk_test`

## AI-ARCH audit

2026-06-05 复核：本文档已按最终 `deps(//app:app, 2)` query 输出更新。最终 query 未引入新的跨层依赖；`AppRepositoryProvider.kt` 仅属于 app 内部组合根源文件。
