# Final Bazel Delivery Evidence

- Date: 2026-06-05 18:43:13 +08:00
- Working directory: D:\feishu-app\ByteTrain_feishu
- Verification entry: `scripts/verify-final-bazel-delivery.ps1`
- Gradle: not used for final acceptance.
- iOS/Xcode: skipped because this repository is Android-only.

## Android app Bazel build

- Status: PASS
- Exit code: 0
- Command: `bazel --batch build //app:app --curses=no --show_progress_rate_limit=60 --jobs=4`

Output summary:

```text
INFO: Found 1 target...
Target //app:app up-to-date:
INFO: Elapsed time: 50.829s, Critical Path: 3.70s
INFO: Build completed successfully, 1 total action
```

## Proto Bazel build

- Status: PASS
- Exit code: 0
- Command: `bazel build //proto:... --curses=no --show_progress_rate_limit=60 --jobs=4`

Output summary:

```text
INFO: Found 1 target...
Target //proto:feed_proto up-to-date:
INFO: Elapsed time: 7.696s, Critical Path: 0.07s
INFO: Build completed successfully, 1 total action
```

## Shared and feature Kotlin Bazel build

- Status: PASS
- Exit code: 0
- Command: `bazel --batch build //shared/list:list //shared/navigation:navigation //shared/ui:ui_models //features/message:domain //features/message:data //features/message:mapper //features/message:ui //features/message:message //features/mail:domain //features/mail:data //features/mail:mapper //features/mail:ui //features/mail:mail --curses=no --show_progress_rate_limit=60 --jobs=4`

Output summary:

```text
INFO: Found 13 targets...
INFO: Elapsed time: 7.781s, Critical Path: 0.25s
INFO: Build completed successfully, 1 total action
```

## Rust SDK Bazel test

- Status: PASS
- Exit code: 0
- Command: `bazel --batch test //sdk/rust:bytetrain_feed_sdk_test --curses=no --show_progress_rate_limit=60 --jobs=4`

Output summary:

```text
INFO: Found 1 test target...
Target //sdk/rust:bytetrain_feed_sdk_test up-to-date:
INFO: Elapsed time: 4.767s, Critical Path: 0.43s
INFO: Build completed successfully, 1 total action
//sdk/rust:bytetrain_feed_sdk_test                              (cached) PASSED in 0.2s
```

## App dependency query

- Status: PASS
- Exit code: 0
- Command: `bazel --batch query --notool_deps --noimplicit_deps --output=label_kind --curses=no "deps(//app:app, 2)"`

Output summary:

```text
android_binary rule //app:app
kt_android_library rule //app:app_lib
kt_jvm_library rule //features/mail:data
kt_jvm_library rule //features/mail:domain
kt_jvm_library rule //features/mail:mapper
kt_android_library rule //features/mail:ui
kt_jvm_library rule //features/message:data
kt_jvm_library rule //features/message:domain
kt_jvm_library rule //features/message:mapper
kt_android_library rule //features/message:ui
kt_jvm_library rule //shared/navigation:navigation
kt_jvm_library rule //shared/ui:ui_models
```

