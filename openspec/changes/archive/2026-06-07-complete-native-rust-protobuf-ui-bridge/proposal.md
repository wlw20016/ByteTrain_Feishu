## Why

当前项目已经定义了 `proto/*.proto`，Rust SDK 也实现了本地分页、async API 和 protobuf bytes 编解码；但 Android UI 运行时并没有真正通过 protobuf 与 Rust SDK 通信，而是通过 Kotlin `RuntimeMessageSdkClient` / `RuntimeMailSdkClient` 模拟同构语义。

课题描述要求“SDK 和 UI 使用 protobuf 通信”“Rust SDK 负责数据解析、异步读取和 protobuf 通信”“使用 Rust 开发 SDK，解析数据并返回给上层 UI”。因此需要新增一个变更，补齐从 UI repository 到 Rust SDK 的真实 protobuf 通信链路。

## What Changes

- 新增 Android SDK bridge，使 `MessageSdkClient` 和 `MailSdkClient` 可以通过 protobuf bytes 调用 Rust SDK。
- 新增 Rust FFI 或等价 native bridge 入口，接收 `PageRequest` protobuf bytes，返回 `MessagePageResponse` / `MailPageResponse` protobuf bytes。
- 将当前 Kotlin `RuntimeMessageSdkClient` / `RuntimeMailSdkClient` 从默认主路径降级为 fallback 或测试 fake。
- 保留 `SdkMessageRepository` / `SdkMailRepository` 的 UI 隔离边界，UI screen 仍不直接依赖 Rust、JNI/FFI 或 protobuf 生成类型。
- 明确 native bridge 的错误协议，区分请求校验、protobuf 编解码、Rust SDK 读取和 bridge 调用失败。
- 增加端到端测试，验证 UI-facing repository 的分页请求确实经过 protobuf bytes 和 Rust SDK 处理。
- 更新中文文档，记录真实实现状态、bridge 调用链、限制和验证命令。

## Capabilities

### New Capabilities

- `native-rust-protobuf-ui-bridge`: 定义 Android UI repository 通过 protobuf bytes 与 Rust SDK native bridge 通信的能力。

### Modified Capabilities

- `sdk-contract`: 将“共享 protobuf 数据契约”升级为“SDK-backed 主路径必须实际使用 protobuf bytes 作为 UI 与 Rust SDK 的通信载体”。
- `sdk-runtime-integration`: 将“runtime bridge 与 protobuf page contract 语义对齐”升级为“runtime bridge 必须调用 Rust SDK bridge 并完成 protobuf request/response 编解码”。

## Impact

- `sdk/rust/src/*`
- `sdk/rust/BUILD.bazel`
- `proto/*.proto`
- `features/message/data/*`
- `features/mail/data/*`
- `app/src/main/kotlin/com/bytetrain/feishuclone/AppRepositoryProvider.kt`
- Android native/JNI/FFI 相关 Gradle 或 Bazel 配置
- `app/src/test` 或新的 integration test
- `docs/ai-context/sdk/*`
- `openspec/specs/sdk-contract/spec.md`
- `openspec/specs/sdk-runtime-integration/spec.md`
