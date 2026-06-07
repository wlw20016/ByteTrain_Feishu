# SDK adapter evidence

This document records the current production SDK-backed adapter path after
`complete-native-rust-protobuf-ui-bridge`.

## Runtime path

The app runtime constructs message and mail repositories through
`AppRepositoryProvider`:

```text
MainActivity
  -> AppRepositoryProvider()
  -> SdkMessageRepository(NativeProtobufMessageSdkClient(), MockMessageRepository())
  -> SdkMailRepository(NativeProtobufMailSdkClient(), MockMailRepository())
  -> NativeRustFeedBridgeClient
  -> NativeRustFeedBridge
  -> JNI bytes bridge
  -> Rust SDK bridge
  -> Rust SDK pagination/protobuf helpers
```

UI screens still depend only on `MessageRepository` and `MailRepository`.
They do not import SDK DTOs, protobuf DTOs, JNI APIs, or Rust types.

## Production client boundary

Production message and mail clients are:

- `features/message/data/NativeProtobufMessageSdkClient.kt`
- `features/mail/data/NativeProtobufMailSdkClient.kt`

Both clients use `NativeRustFeedBridgeClient` to encode `PageRequest` bytes,
call the native Rust bridge, decode `MessagePageResponse` or
`MailPageResponse` bytes, and map SDK DTO fields/enums back to repository
domain models.

`shared/sdk/NativeRustFeedBridge.kt` owns `System.loadLibrary("bytetrain_feed_sdk")`
and the JNI calls. Native bridge responses use a small envelope:

- tag `0`: success, followed by protobuf response bytes
- tag `1`: failure, followed by structured error code and UTF-8 message

`SdkBridgeException` carries `BridgeErrorCode` so repository fallback can handle
native bridge, protobuf, cursor, page-size, or SDK read failures.

## Fallback and runtime fake boundary

`AppRepositoryProvider` now defaults to native protobuf clients. Mock fallback
repositories remain configured so UI flows survive native construction or load
failures.

`RuntimeMessageSdkClient` and `RuntimeMailSdkClient` remain useful deterministic
Kotlin fakes, but they are not production Rust protobuf bridge evidence. They
are reachable only through explicit test injection or
`AppRepositoryProvider.withRuntimeFakeFallback()`.

## Rust bridge boundary

Rust bridge APIs in `sdk/rust/src/bridge.rs` accept `PageRequest` bytes and
return response bytes:

```rust
read_message_page_response_bytes(request_bytes: &[u8]) -> BridgeResult<Vec<u8>>
read_mail_page_response_bytes(request_bytes: &[u8]) -> BridgeResult<Vec<u8>>
```

The bridge decodes requests through Rust SDK protobuf helpers, reads pages
through `MockFeedSdk`, encodes responses through Rust SDK protobuf helpers, and
maps SDK errors to structured `BridgeErrorCode` values.

JNI exports:

```text
Java_com_bytetrain_feishuclone_sdk_NativeRustFeedBridge_readMessagePageNative
Java_com_bytetrain_feishuclone_sdk_NativeRustFeedBridge_readMailPageNative
```

## Native packaging

`app/build.gradle.kts` builds Rust Android `cdylib` artifacts for:

- `arm64-v8a`
- `armeabi-v7a`
- `x86`
- `x86_64`

The generated `.so` files are copied into `app/build/rustJniLibs` and included
as app `jniLibs`.

APK inspection after `.\gradlew.bat assembleDebug`:

```text
lib/arm64-v8a/libbytetrain_feed_sdk.so
lib/armeabi-v7a/libbytetrain_feed_sdk.so
lib/x86/libbytetrain_feed_sdk.so
lib/x86_64/libbytetrain_feed_sdk.so
```

## Verification evidence

Verified on 2026-06-08:

```powershell
cargo test --manifest-path sdk/rust/Cargo.toml
bazel.cmd --batch test //sdk/rust:bytetrain_feed_sdk_test
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
jar tf app\build\outputs\apk\debug\app-debug.apk | Select-String -Pattern "libbytetrain_feed_sdk"
```

Results:

- Rust cargo tests: 17 passed, 0 failed.
- Bazel Rust test: `//sdk/rust:bytetrain_feed_sdk_test` passed.
- Android unit tests: `BUILD SUCCESSFUL`.
- Android debug APK: `BUILD SUCCESSFUL`.
- APK contains all four ABI copies of `libbytetrain_feed_sdk.so`.

## Remaining boundary

This change verifies build-time packaging and JVM-level repository/native-client
behavior. It does not include a device or emulator smoke test that launches the
APK and calls `System.loadLibrary` on Android runtime hardware.
