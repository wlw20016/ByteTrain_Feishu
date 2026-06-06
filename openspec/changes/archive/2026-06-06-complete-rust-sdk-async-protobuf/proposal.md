## Why

当前 SDK runtime 路径已经证明分页语义和 protobuf 形态契约，但还没有提供 Rust async 接口，也没有真实的 protobuf 编解码边界。课题要求明确提到 Rust SDK 负责数据解析、异步读取和 protobuf 通信，因此本 change 用独立步骤补齐这部分能力，同时不把 native Android FFI 强行并入同一阶段。

## What Changes

- 新增 Rust SDK request/response 类型，直接对齐现有 `proto/paging.proto`、`proto/message.proto` 和 `proto/mail.proto` 契约。
- 新增消息和邮箱分页读取的 Rust async SDK API。
- 在 SDK 边界新增 protobuf 序列化和反序列化支持。
- 新增文档化错误模型，区分非法请求、编解码失败、传输/读取失败和 cursor 越界。
- 新增 Rust 单测，覆盖 async 分页读取、protobuf round trip 和错误 case。
- 更新 AI context 文档，记录协议形态、异步接口示例和 Android 集成剩余限制。

## Capabilities

### New Capabilities

- `rust-sdk-async-protobuf`：Rust SDK async 分页读取和 protobuf 边界。

### Modified Capabilities

- `sdk-runtime-integration`：SDK runtime integration 将引用具体 Rust async/protobuf 边界，同时允许 Android 在 native transport 完成前继续使用当前 Kotlin bridge。

## Impact

- `sdk/rust/Cargo.toml`
- `sdk/rust/src/lib.rs` or new Rust SDK modules
- `proto/*.proto`
- `sdk/rust/BUILD.bazel`
- `docs/ai-context/sdk/sdk-adapter-evidence.md`
- `docs/ai-context/build-system/build-commands.md`
- OpenSpec SDK runtime specs and tasks
