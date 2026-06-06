## Overview

本 change 将 Rust SDK 从同步、确定性的分页库升级为可验证的 async/protobuf SDK 边界。本阶段仍保持本地和确定性实现，但公开 API 必须准确表达后续真实服务边界。

## Scope

范围内：

- 消息和邮箱分页读取的 Rust async API。
- protobuf request/response 编码和解码 helper。
- 覆盖请求校验、protobuf 转换和读取失败的结构化 Rust SDK error。
- Rust 测试和 Bazel 测试覆盖。
- AI 可读文档和示例。

范围外：

- Native Android FFI。
- 真实网络传输。
- 将 generated Kotlin protobuf runtime 接入 Android UI。
- 替换 `MainActivity` 当前 Kotlin runtime bridge。

## API Shape

推荐 Rust API 形态：

```rust
pub async fn read_message_page(request: PageRequest) -> SdkResult<MessagePageResponse>;
pub async fn read_mail_page(request: PageRequest) -> SdkResult<MailPageResponse>;
```

如果引入 generated Rust protobuf types，应将其隔离在 SDK-facing helpers 后面，让测试同时验证 domain pagination 和 protobuf 序列化 round trip。

## Error Model

SDK 应区分：

- 非法 page size。
- 非法 cursor 格式。
- cursor 超出数据范围。
- protobuf decode 失败。
- protobuf encode 失败。
- async read 或 transport 失败。

错误必须可在测试中通过类型或字段匹配，不依赖解析 display string。

## Protobuf Boundary

Rust SDK 应对齐：

- `PageRequest.page_size`
- `PageRequest.cursor`
- `PageInfo.next_cursor`
- `PageInfo.has_more`
- `MessagePageResponse.items`
- `MailPageResponse.items`

当前 Rust domain model 中不存在的 mail 字段，必须补进 Rust model，或记录为明确转换默认值。

## Bazel

Rust Bazel target 应继续暴露：

- `//sdk/rust:bytetrain_feed_sdk`
- `//sdk/rust:bytetrain_feed_sdk_test`

如果 protobuf code generation 新增 generated sources 或依赖，必须更新 `sdk/rust/BUILD.bazel` 和 `MODULE.bazel`，并在 `docs/ai-context/build-system/build-commands.md` 中记录对应 target 和命令。

## Verification

最小验证：

```powershell
cargo test --manifest-path sdk/rust/Cargo.toml
bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4
```

测试必须覆盖：

- async 读取第一页。
- async 读取下一页。
- 非法 cursor。
- 越界 cursor。
- 非法 page size。
- message protobuf round trip。
- mail protobuf round trip。
- decode failure。
