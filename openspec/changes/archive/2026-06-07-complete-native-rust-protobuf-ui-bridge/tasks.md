## 1. Status Confirmation And Boundaries

- [x] 1.1 Record current real implementation status: proto contracts were already defined, Rust SDK local async/protobuf helpers were already implemented, and Android UI was not yet using protobuf bytes to call Rust SDK before this change.
- [x] 1.2 Confirm production SDK-backed target chain: `AppRepositoryProvider -> Sdk*Repository -> NativeProtobuf*SdkClient -> NativeRustFeedBridgeClient -> NativeRustFeedBridge -> JNI Rust bridge -> Rust SDK`.
- [x] 1.3 Clarify `RuntimeMessageSdkClient` and `RuntimeMailSdkClient` are fallback/test fakes only, not evidence of real Rust protobuf communication.

## 2. Rust SDK Bridge

- [x] 2.1 Add Rust bridge-level message page bytes API that accepts `PageRequest` bytes and returns `MessagePageResponse` bytes.
- [x] 2.2 Add Rust bridge-level mail page bytes API that accepts `PageRequest` bytes and returns `MailPageResponse` bytes.
- [x] 2.3 Add structured bridge error results covering protobuf decode, protobuf encode, invalid page size, invalid cursor, cursor out of range, SDK read failure, and native bridge failure.
- [x] 2.4 Add Rust tests for message first page, message next page, mail first page, mail next page, and bridge error cases.
- [x] 2.5 Update `sdk/rust/BUILD.bazel` coverage so Rust bridge code is included in the Bazel target through `glob(["src/**/*.rs"])`.

## 3. Android Native Protobuf Client

- [x] 3.1 Add production `MessageSdkClient` implementation that calls the Rust message bridge through protobuf bytes.
- [x] 3.2 Add production `MailSdkClient` implementation that calls the Rust mail bridge through protobuf bytes.
- [x] 3.3 Complete protobuf request encode, response decode, and SDK DTO/domain enum mapping inside Kotlin clients.
- [x] 3.4 Map native bridge errors into fallback-compatible Kotlin exceptions through `SdkBridgeException` and `BridgeErrorCode`.
- [x] 3.5 Update Gradle native library configuration so Android debug builds produce and package Rust bridge `.so` files for `arm64-v8a`, `armeabi-v7a`, `x86`, and `x86_64`.

## 4. Runtime Wiring

- [x] 4.1 Update `AppRepositoryProvider` default factories to prefer native protobuf SDK clients.
- [x] 4.2 Preserve mock repository fallback so native client construction/load failure does not break the UI flow.
- [x] 4.3 Preserve Kotlin runtime clients as explicit test fake/fallback clients and document that they are not real Rust bridge evidence.

## 5. Verification

- [x] 5.1 Add Kotlin test: with fallback disabled, message repository loads first page through `NativeProtobufMessageSdkClient` and protobuf bytes bridge injection.
- [x] 5.2 Add Kotlin test: with fallback disabled, mail repository loads first page through `NativeProtobufMailSdkClient` and protobuf bytes bridge injection.
- [x] 5.3 Add Kotlin tests: native bridge protobuf/cursor errors trigger repository fallback as expected.
- [x] 5.4 Run `cargo test --manifest-path sdk/rust/Cargo.toml` and record result.
- [x] 5.5 Run `bazel.cmd --batch test //sdk/rust:bytetrain_feed_sdk_test` and record result.
- [x] 5.6 Run Android unit/build verification proving app packaging includes the native bridge library.

## 6. Documentation And Evidence

- [x] 6.1 Update `docs/ai-context/sdk/sdk-adapter-evidence.md` to describe that the real Rust protobuf bridge is now wired into the production path.
- [x] 6.2 Update Rust SDK + Protobuf design documentation to distinguish proto contract, local Rust helpers, real UI communication chain, and fallback/fake behavior.
- [x] 6.3 Record key verification commands, test results, and remaining limits in this OpenSpec tasks file.

## Verification Results

Verified on 2026-06-08:

```powershell
cargo test --manifest-path sdk/rust/Cargo.toml
bazel.cmd --batch test //sdk/rust:bytetrain_feed_sdk_test
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
jar tf app\build\outputs\apk\debug\app-debug.apk | Select-String -Pattern "libbytetrain_feed_sdk"
```

Results:

- `cargo test --manifest-path sdk/rust/Cargo.toml`: 17 passed, 0 failed.
- `bazel.cmd --batch test //sdk/rust:bytetrain_feed_sdk_test`: passed.
- `.\gradlew.bat testDebugUnitTest`: `BUILD SUCCESSFUL`.
- `.\gradlew.bat assembleDebug`: `BUILD SUCCESSFUL`; Android Rust cdylibs built for all four configured ABIs.
- APK contains:
  - `lib/arm64-v8a/libbytetrain_feed_sdk.so`
  - `lib/armeabi-v7a/libbytetrain_feed_sdk.so`
  - `lib/x86/libbytetrain_feed_sdk.so`
  - `lib/x86_64/libbytetrain_feed_sdk.so`

Remaining limit:

- This session did not run a device/emulator launch smoke test. Build-time packaging and JVM-level repository/native-client behavior are verified; runtime `System.loadLibrary` execution on an Android device remains the next integration check.
