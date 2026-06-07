# Rust SDK + Protobuf 设计文档

本文档说明当前的 Protobuf 契约、本地 Rust SDK 辅助函数、真实的 UI 通信路径以及降级（fallback）路径。

## 分层架构

```text
proto/*.proto
  -> Rust SDK 模型、分页逻辑、异步辅助函数、Protobuf 字节辅助函数
  -> Rust 桥接层字节 API 和 JNI 导出函数
  -> Kotlin NativeRustFeedBridgeClient
  -> NativeProtobufMessageSdkClient / NativeProtobufMailSdkClient
  -> SdkMessageRepository / SdkMailRepository
  -> UI 层依赖的 MessageRepository / MailRepository
```

UI 层通过 Repository 接口与 Protobuf 和 native 细节隔离。

## Protobuf 契约

共享的 schema 定义在：

- `proto/paging.proto`
- `proto/message.proto`
- `proto/mail.proto`

核心消息：

```proto
message PageRequest {
  int32 page_size = 1;
  string cursor = 2;
}

message PageInfo {
  string next_cursor = 1;
  bool has_more = 2;
}
```

`MessagePageResponse` 和 `MailPageResponse` 携带重复的 item 消息以及 `PageInfo`。

## Rust SDK 辅助函数

Rust 使用强类型内部模型（`Page<T>`、`PageRequest`、`MessageItem`、`MailItem`），并在边界处与 Protobuf 字节进行互转。

重要的 Rust 辅助函数：

```rust
encode_page_request
decode_page_request
encode_message_page_response
decode_message_page_response
encode_mail_page_response
decode_mail_page_response
```

分页规则：

- `page_size` 必须在 `1..=200` 之间。
- 空或缺失的 cursor 表示请求第一页。
- 当前的 cursor 是十进制字符串表示的、从 0 开始的起始下标。
- 仅当 `has_more` 为 true 时才会设置 `next_cursor`。

## Rust 桥接层

`sdk/rust/src/bridge.rs` 提供了桥接层字节 API：

```rust
read_message_page_response_bytes(request_bytes: &[u8]) -> BridgeResult<Vec<u8>>
read_mail_page_response_bytes(request_bytes: &[u8]) -> BridgeResult<Vec<u8>>
```

桥接层负责 Protobuf 请求解码、Rust SDK 分页读取、响应编码以及结构化错误映射。

JNI 导出函数返回一个信封结构（envelope），以便 Kotlin 区分成功和桥接失败：

- 成功：tag `0` + 响应字节
- 错误：tag `1` + 桥接错误码 + 消息长度 + 消息字节

错误码涵盖：无效的 page size、无效的 cursor、cursor 越界、Protobuf 解码错误、Protobuf 编码错误、SDK 读取错误、native 桥接失败。

## Kotlin 生产路径

`NativeProtobufMessageSdkClient` 和 `NativeProtobufMailSdkClient` 是生产的 SDK 客户端。它们执行以下步骤：

1. 编码 `PageRequest` 为 Protobuf 字节。
2. 调用 `NativeRustFeedBridgeClient`。
3. 解码 Rust 响应的 Protobuf 字节。
4. 将 Protobuf 字段映射到 SDK DTO / 领域枚举值。
5. 由 `SdkMessageRepository` / `SdkMailRepository` 将 SDK DTO 映射为 UI 所需的领域模型。

`AppRepositoryProvider` 默认使用这些 native protobuf 客户端。

## 降级与 fake 路径

Mock 仓库的降级路径被有意保留。如果 native 客户端构造或 native 桥接调用失败，Repository 适配器会将相同的 `pageSize` 和 `cursor` 请求委托给降级仓库。

`RuntimeMessageSdkClient` 和 `RuntimeMailSdkClient` 保留了确定性的 Kotlin fake 实现，用于测试或显式降级构造。它们不能被描述为真实的 Rust Protobuf 通信证据。

## 构建集成

`sdk/rust/Cargo.toml` 同时构建 `rlib` 和 `cdylib`。

`app/build.gradle.kts` 为 `arm64-v8a`、`armeabi-v7a`、`x86` 和 `x86_64` 构建 Android Rust cdylib，将它们复制到生成的 `jniLibs` 中，并打包进 debug APK。

`sdk/rust/BUILD.bazel` 使用 `glob(["src/**/*.rs"])`，因此 `bridge.rs` 包含在 Bazel Rust 库/测试目标中。

## 验证结果

已于 2026-06-08 验证：

- `cargo test --manifest-path sdk/rust/Cargo.toml`：17 个测试通过。
- `bazel.cmd --batch test //sdk/rust:bytetrain_feed_sdk_test`：通过。
- `.\gradlew.bat testDebugUnitTest`：通过。
- `.\gradlew.bat assembleDebug`：通过。
- APK 中包含了针对全部四种配置 ABI 的 `libbytetrain_feed_sdk.so`。

## 剩余限制

在本会话中，仓库尚未在 Android 真机/模拟器上运行冒烟测试。构建时的打包和 JVM 级别的客户端/仓库行为已验证通过；在设备上运行时的 `System.loadLibrary` 执行是下一个集成检查点。