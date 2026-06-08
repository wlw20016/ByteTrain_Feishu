# Full Bazel Workspace Evidence

- Date: 2026-06-08 03:00:32 +08:00
- Working directory: D:\feishu-app\ByteTrain_feishu
- Verification entry: `scripts/commands/verify-full-bazel-workspace.ps1`
- Bazel executable: `bazel`
- PowerShell: 5.1.26100.8457
- OS: Microsoft Windows NT 10.0.26200.0
- Acceptance build targets: `//...`
- Acceptance test targets: `//...`
- Allowed exclusions: none.
- Gradle: not used for full-workspace acceptance.
- iOS/Xcode: skipped because this repository is Android-only.
- Access-denied handling: access-denied output is treated as failure even if the shell does not propagate a native non-zero exit code.

## Full workspace Bazel build

- Status: PASS
- Exit code: 0
- Command: `bazel --batch build //... --curses=no --show_progress_rate_limit=60 --jobs=4`

Output summary:

```text
INFO: Analyzed 25 targets (362 packages loaded, 23228 targets configured, 37 aspect applications).
INFO: Found 25 targets...
INFO: Elapsed time: 11.223s, Critical Path: 1.39s
INFO: Build completed successfully, 1 total action
```

## Full workspace Bazel test

- Status: PASS
- Exit code: 0
- Command: `bazel --batch test //... --curses=no --show_progress_rate_limit=60 --jobs=4`

Output summary:

```text
INFO: Analyzed 25 targets (362 packages loaded, 23228 targets configured, 37 aspect applications).
INFO: Found 24 targets and 1 test target...
INFO: Elapsed time: 11.030s, Critical Path: 1.10s
INFO: Build completed successfully, 1 total action
//sdk/rust:bytetrain_feed_sdk_test                              (cached) PASSED in 0.2s
Executed 0 out of 1 test: 1 test passes.
```

## Full workspace Bazel rule query

- Status: PASS
- Exit code: 0
- Command: `bazel --batch query --output=label_kind --curses=no "kind('.* rule', //...)"`

Output summary:

```text
Rule targets: 25
android_binary rule //app:app
kt_android_library rule //app:app_lib
genrule rule //app:run_app
kt_jvm_library rule //features/mail:data
kt_jvm_library rule //features/mail:domain
kt_jvm_library rule //features/mail:mail
kt_jvm_library rule //features/mail:mapper
kt_android_library rule //features/mail:ui
kt_jvm_library rule //features/message:data
kt_jvm_library rule //features/message:domain
kt_jvm_library rule //features/message:mapper
kt_jvm_library rule //features/message:message
kt_android_library rule //features/message:ui
alias rule //proto:...
proto_library rule //proto:feed_proto
proto_library rule //proto:mail_proto
proto_library rule //proto:message_proto
proto_library rule //proto:paging_proto
rust_library rule //sdk/rust:bytetrain_feed_sdk
rust_test rule //sdk/rust:bytetrain_feed_sdk_test
kt_jvm_library rule //shared/list:list
kt_jvm_library rule //shared/navigation:navigation
kt_jvm_library rule //shared/sdk:sdk_bridge
kt_android_library rule //shared/ui:ui_android
kt_jvm_library rule //shared/ui:ui_models
```

