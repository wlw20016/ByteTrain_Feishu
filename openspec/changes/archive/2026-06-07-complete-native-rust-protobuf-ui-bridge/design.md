## Context

当前实现状态可以拆成三部分：

- 已实现：`proto/paging.proto`、`proto/message.proto`、`proto/mail.proto` 已定义分页、消息和邮箱契约。
- 已实现：Rust SDK 已有 `PageRequest`、`MessagePageResponse`、`MailPageResponse`，并在 `protobuf.rs` 中提供 `encode_*` / `decode_*` bytes helper；`async_api.rs` 提供本地 async 形态读取。
- 未实现：Android UI 运行时没有 JNI/FFI/native bridge，也没有通过 protobuf bytes 调用 Rust SDK。`AppRepositoryProvider` 当前默认构造的是 `SdkMessageRepository(RuntimeMessageSdkClient(), fallback)` 和 `SdkMailRepository(RuntimeMailSdkClient(), fallback)`，其中 runtime client 是 Kotlin 本地实现。

因此，这个 change 的目标不是重复定义 proto 或重复实现 Rust 本地 protobuf helper，而是补齐“UI repository -> Kotlin SDK client -> protobuf bytes -> Rust SDK -> protobuf bytes -> Kotlin domain mapping”的真实链路。

## Goals / Non-Goals

**Goals:**

- Android SDK-backed 主路径实际调用 Rust SDK。
- Kotlin 与 Rust SDK 的跨语言边界使用 protobuf bytes。
- Rust SDK 继续负责分页数据解析、读取和 protobuf 编解码。
- UI screen 继续只依赖 repository/domain model，不直接依赖 native bridge。
- 保留 mock fallback，native bridge 失败时 UI 主流程不崩溃。
- 用测试证明 repository 请求经过 Rust SDK bridge，而不是只经过 Kotlin runtime fake。

**Non-Goals:**

- 不引入真实网络服务。
- 不把 UI screen 改成直接消费 protobuf DTO。
- 不取消 mock repository fallback。
- 不要求一次性接入 gRPC service；本阶段可以使用 JNI/FFI bytes 函数作为通信边界。

## Decisions

### Decision 1: bridge 边界使用 protobuf bytes

采用 bytes 函数作为跨语言边界：

```text
read_message_page(request_bytes: ByteArray) -> response_bytes: ByteArray
read_mail_page(request_bytes: ByteArray) -> response_bytes: ByteArray
```

理由：

- Rust SDK 已经有 `decode_page_request`、`encode_message_page_response`、`encode_mail_page_response`。
- Kotlin 侧可以在 client 内部封装 protobuf 编解码，repository 和 UI 不需要知道 native 细节。
- bytes 边界比逐字段 JNI 参数更稳定，新增字段时可以利用 Protobuf 的向前兼容。

备选方案：

- 逐字段 JNI 调用：实现简单但扩展差，字段变更会频繁改 native 签名。
- 直接让 UI 消费 protobuf DTO：会污染 UI/领域层边界，不符合现有模块设计。

### Decision 2: 保持 repository adapter 作为 UI 保护层

继续使用 `SdkMessageRepository` / `SdkMailRepository`，只替换其默认 `sdkClient` 实现。新增 native client 后，`AppRepositoryProvider` 默认应构造 native protobuf client；Kotlin runtime client 保留为 fallback 或测试 fake。

理由：

- 当前 UI 已经依赖 `MessageRepository` / `MailRepository`。
- 替换 client 不需要改 UI screen、mapper 和 navigation。
- fallback 策略可以降低 native bridge 集成风险。

### Decision 3: Rust 侧暴露 bridge wrapper，而不是把内部 domain API 直接暴露给 Kotlin

Rust bridge wrapper 负责：

- 接收 request bytes。
- 调用 `decode_page_request`。
- 调用 `read_message_page` / `read_mail_page` 或同语义内部 API。
- 调用 response encode helper。
- 将错误转换成 bridge 可识别的错误结果。

理由：

- Rust domain API 保持可测试、可复用。
- protobuf 失败和读取失败可以在 Rust 层统一分类。
- Kotlin 侧不需要重建 Rust 内部分页逻辑。

### Decision 4: 错误协议必须结构化

bridge 不应只返回 display string。最小可接受形态：

- 成功：response bytes。
- 失败：错误 code + message，或 protobuf/JSON error envelope。

错误分类至少包括：

- invalid page size
- invalid cursor
- cursor out of range
- protobuf decode failure
- protobuf encode failure
- native bridge failure

## Risks / Trade-offs

- Native bridge 增加构建复杂度 -> 先用最小 bytes API，避免一次性引入完整 RPC 框架。
- Android 本地库打包在 Bazel/Gradle 双构建下容易漂移 -> 明确验证命令，并记录成功产物。
- 手写 protobuf 编解码长期维护成本高 -> 本 change 可以继续使用现有 helper，但任务中保留评估 `prost` / generated bindings 的步骤。
- fallback 可能掩盖 native bridge 未生效 -> 测试必须包含“禁用 fallback 时仍能从 Rust bridge 获取第一页”的场景。

## Migration Plan

1. 先在 Rust SDK 增加 bridge-level bytes API 和测试。
2. 再在 Android 侧新增 native protobuf client，实现 `MessageSdkClient` 和 `MailSdkClient`。
3. 修改 `AppRepositoryProvider` 默认 client factory，优先使用 native protobuf client。
4. 保留 Kotlin runtime client 作为 fallback/test fake。
5. 增加验证文档，明确哪些测试证明真实 Rust bridge 已接入。
