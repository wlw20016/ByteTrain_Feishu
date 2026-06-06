# 任务：complete-rust-sdk-async-protobuf

## 1. 协议与 API 设计

- [x] SDK-ASYNC-001 复核现有 `proto/*.proto`，记录 Rust request/response 映射关系。
- [x] SDK-ASYNC-002 定义消息和邮箱分页的 Rust async read API。
- [x] SDK-ASYNC-003 定义覆盖校验、protobuf 转换和 async 读取失败的结构化错误模型。

## 2. Rust SDK 实现

- [x] SDK-ASYNC-004 新增 protobuf encode/decode 支持，或接入 generated Rust protobuf bindings。
- [x] SDK-ASYNC-005 基于现有分页语义实现 async message page read。
- [x] SDK-ASYNC-006 基于现有分页语义实现 async mail page read。
- [x] SDK-ASYNC-007 保留现有同步 helpers，或为当前测试提供文档化迁移路径。

## 3. 测试

- [x] SDK-ASYNC-008 新增 Rust 测试，覆盖 async 第一页和下一页读取。
- [x] SDK-ASYNC-009 新增 Rust 测试，覆盖非法 page size、非法 cursor 和越界 cursor。
- [x] SDK-ASYNC-010 新增 message/mail response 的 protobuf round-trip 测试。
- [x] SDK-ASYNC-011 新增 protobuf decode failure 测试。

## 4. Bazel

- [x] SDK-ASYNC-012 更新 `sdk/rust/BUILD.bazel` 和必要 module 依赖。
- [x] SDK-ASYNC-013 运行 `cargo test --manifest-path sdk/rust/Cargo.toml` 并记录结果。
- [x] SDK-ASYNC-014 运行 `bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4` 并记录结果。

## 5. 文档

- [x] SDK-ASYNC-015 更新 SDK/protobuf AI context，记录协议映射、async interface 示例、测试和剩余限制。
- [x] SDK-ASYNC-016 如果 Bazel 命令或 target 发生变化，更新 `docs/ai-context/build-commands.md`。
