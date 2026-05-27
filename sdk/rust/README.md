# Rust SDK

用于数据解析、异步数据访问和后续 protobuf 边界的 Rust SDK 骨架。

职责：

- 生成或读取消息和邮件 mock 数据。
- 提供面向分页的异步 API。
- 在暴露 FFI 之前定义错误模型。
- 在后续 change 中接入 protobuf 契约。

规划中的 Bazel target：

```text
//sdk/rust:feed_sdk
```
