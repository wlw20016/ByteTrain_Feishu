# 任务：connect-sdk-protobuf-runtime

## 1. 运行时边界

- [x] SDK-RT-001 新增 app 级仓库提供器，能够构造由 SDK 支撑的消息和邮箱仓库，并保留 mock 回退路径。
- [x] SDK-RT-002 为当前运行时桥接新增具体的 `MessageSdkClient` 和 `MailSdkClient` 实现。
- [x] SDK-RT-003 将 `MainActivity` 或 app 组合根改为使用仓库提供器，避免直接实例化 mock 仓库。

## 2. Protobuf 对齐

- [x] SDK-RT-004 验证 `message.proto`、`mail.proto` 和 `paging.proto` 覆盖 Kotlin 领域模型与 Rust SDK 分页响应所需的全部字段。
- [x] SDK-RT-005 新增或记录运行时桥接使用的 protobuf 形态请求/响应映射。
- [x] SDK-RT-006 在运行时映射引入后补充 Bazel proto 验证证据。

## 3. 测试与证据

- [x] SDK-RT-007 新增聚焦检查，覆盖由 SDK 支撑的消息第一页、下一页、非法 cursor 回退和关键字段映射。
- [x] SDK-RT-008 新增聚焦检查，覆盖由 SDK 支撑的邮箱第一页、下一页、非法 cursor 回退和关键字段映射。
- [x] SDK-RT-009 运行 `cargo test --manifest-path sdk/rust/Cargo.toml` 并记录结果。
- [x] SDK-RT-010 运行 app、proto 和 SDK 构建目标相关 Bazel build/test 命令；如果被环境阻塞，记录精确阻塞、根因和重试命令。

## 4. 文档

- [x] SDK-RT-011 更新 `docs/ai-context/sdk/sdk-adapter-evidence.md`，记录最终运行时路径和剩余限制。
- [x] SDK-RT-012 在标记完成前，更新本 `tasks.md`，补充命令输出摘要和实现证据。

## 实现证据

### SDK-RT-001

- 实现：新增 app 层 `AppRepositoryProvider`，作为 `MessageRepository` 与 `MailRepository` 的组合根构造入口。
- SDK-backed 路径：默认 `sdkRuntimeEnabled = true`，`createMessageRepository()` 构造 `SdkMessageRepository(RuntimeMessageSdkClient(), fallback)`，`createMailRepository()` 构造 `SdkMailRepository(RuntimeMailSdkClient(), fallback)`。
- Mock 回退：provider 总是先构造 `MockMessageRepository` / `MockMailRepository` 作为 fallback；当 `sdkRuntimeEnabled = false` 或 SDK client / SDK repository 构造失败时，直接返回 mock fallback。
- 运行时回退：SDK-backed repository 内仍保留 fallback repository，因此后续分页加载发生 SDK runtime 错误时可委托 mock fallback。
- 验证命令：`.\\gradlew.bat :app:compileDebugKotlin` 成功，输出 `BUILD SUCCESSFUL in 1s`。
- 任务边界：`MainActivity` 切换到 provider 的接入留给 `SDK-RT-003`，本任务只新增 app 级 provider。

### SDK-RT-002

- 实现：新增 `RuntimeMessageSdkClient`，实现 `MessageSdkClient`，在当前 Kotlin 运行时提供 SDK 形态消息分页数据。
- 实现：新增 `RuntimeMailSdkClient`，实现 `MailSdkClient`，在当前 Kotlin 运行时提供 SDK 形态邮箱分页数据。
- 运行时语义：两个 client 均按 Rust SDK 语义校验 `pageSize in 1..200`；`cursor == null` 或空字符串请求第一页；非空 cursor 作为零基 start index 解析；非法 cursor、越界 cursor 和非法 page size 抛桥接错误，供 repository fallback 路径接管。
- 响应语义：`nextCursor` 仅在 `hasMore == true` 时返回下一页起点，否则为 `null`；消息字段复刻 Rust SDK 的 deterministic message 数据；邮箱字段复刻 Rust SDK 基础字段，并按 `SDK-RT-005` 映射规则为 Rust-only mail 页提供 `attachmentCount = 0`、`mailType = UPDATE`、`actionText = null`。
- 构建入口：已将 `RuntimeMessageSdkClient.kt` 加入 `//features/message:data`，将 `RuntimeMailSdkClient.kt` 加入 `//features/mail:data`。
- 验证命令：`.\\gradlew.bat :app:compileDebugKotlin` 成功，输出 `BUILD SUCCESSFUL in 11s`。
- 验证限制：`bazelisk build //features/message:data //features/mail:data` 获权运行后 124s 超时，已停止残留 `bazelisk` 进程；完整 Bazel 证据留待 `SDK-RT-010`。

### SDK-RT-003

- 接入：`MainActivity` 已移除对 `MockMessageRepository` / `MockMailRepository` 的直接 import 和直接实例化。
- 接入：`MainActivity` 现在创建 `AppRepositoryProvider()`，并通过 `createMessageRepository()` / `createMailRepository()` 获取 UI-facing repositories。
- 运行时路径：默认 provider 会构造 `SdkMessageRepository(RuntimeMessageSdkClient(), fallback)` 和 `SdkMailRepository(RuntimeMailSdkClient(), fallback)`，因此 UI 分页加载已通过 SDK-backed repository 路径，同时保留 mock fallback。
- UI 文案：列表统计文案从 `mock conversations` / `mock emails` 调整为通用 `conversations` / `emails`，避免运行时路径切到 SDK-backed 后仍显示 mock。
- 验证命令：`.\\gradlew.bat :app:compileDebugKotlin` 成功，输出 `BUILD SUCCESSFUL in 3s`。

### SDK-RT-004

- 字段对齐：`message.proto` 已覆盖 Kotlin `MessageItem` / `SdkMessageItem` 的 `id`、`conversationName`、`conversationType`、`avatarUrl`、`avatarText`、`lastMessagePreview`、`lastMessageTimeMillis`、`unreadCount`、`isPinned`、`isMuted`、`isBot`，并通过 `ConversationType` 覆盖 `SINGLE`、`GROUP`、`BOT`。
- 字段对齐：`mail.proto` 原有字段覆盖 Rust SDK `MailItem`，本次追加 `MailType`、`attachment_count`、`mail_type`、`action_text`，覆盖 Kotlin `MailItem` / `SdkMailItem` 的 `attachmentCount`、`mailType`、`actionText`。
- 分页对齐：`paging.proto` 的 `PageRequest.page_size` / `cursor` 覆盖 SDK 请求语义；`PageInfo.next_cursor` / `has_more` 与 `MessagePageResponse.items`、`MailPageResponse.items` 覆盖 Rust `Page<T>` 的 `items`、`next_cursor`、`has_more`。
- 验证命令：`rg -n "enum MailType|attachment_count|mail_type|action_text|enum ConversationType|PageRequest|PageInfo|next_cursor|has_more" proto features sdk` 成功确认关键字段存在。
- 验证限制：`protoc --version` 当前环境不可用；`bazelisk build //proto:feed_proto` 获权运行后 124s 超时，完整 Bazel proto 构建证据留待 `SDK-RT-006`。

### SDK-RT-005

- 映射记录：已在 `design.md` 的 `Protobuf Contract Shape / Runtime Mapping Record` 补充运行时桥接映射。
- 请求映射：`MessageSdkClient.getMessagePage(pageSize, cursor)` 与 `MailSdkClient.getMailPage(pageSize, cursor)` 对齐 `PageRequest.page_size` / `cursor`，明确 `null` 和空 cursor 请求第一页，非空 cursor 原样透传。
- 响应映射：记录 `MessagePageResponse`、`MailPageResponse`、`PageInfo` 到 `SdkMessagePage` / `SdkMailPage` 以及领域 page 的字段对应关系。
- 字段规则：记录 snake_case 到 camelCase 的字段名转换、空字符串到 Kotlin nullable 字段的 `null` 转换、枚举一一映射、Rust-only mail 页的 `attachmentCount = 0` / `mailType = UPDATE` 默认策略。
- 失败映射：记录非法 `page_size`、非法 cursor、越界 cursor、非法枚举和畸形响应字段作为桥接失败；有 fallback repository 时委托 fallback，无 fallback 时向调用方抛出。

### SDK-RT-006

- 验证目标：`//proto:feed_proto`、`//proto:paging_proto`、`//proto:message_proto`、`//proto:mail_proto`，覆盖运行时映射依赖的 `MailPageResponse`、`MessagePageResponse`、`PageRequest`、`PageInfo`。
- 前置阻塞：`bazelisk --batch build //proto:feed_proto --verbose_failures` 未进入构建阶段，原因是没有 `.bazelversion`，`bazelisk` 尝试联网解析 Bazel `latest`，访问 `https://www.googleapis.com/storage/v1/b/bazel/o?delimiter=/` 30s 超时。
- 前置阻塞：本地 `C:\Users\23064\tools\bazel-9.1.0\bazel.exe` 无法使用，报错 `Error reading zip file ... local file header signature for file embedded_tools/jdk/bin/sunmscapi.dll not found`。
- 成功命令：`C:\Users\23064\.bazelisk\downloads\sha256\2db62663b47eb90143932cd553f66840dc03da8e7ce0a23a1302e63fdc234254\bin\bazel.exe --batch build //proto:feed_proto --verbose_failures`。
- 结果摘要：Bazel 分析目标 `//proto:feed_proto` 成功，输出 `Target //proto:feed_proto up-to-date: bazel-bin/proto/feed_proto-descriptor-set.proto.bin`。
- 构建结果：`INFO: Build completed successfully, 2 total actions`，耗时 `95.687s`。
- 补充命令：`C:\Users\23064\.bazelisk\downloads\sha256\2db62663b47eb90143932cd553f66840dc03da8e7ce0a23a1302e63fdc234254\bin\bazel.exe --batch build //proto:paging_proto //proto:message_proto //proto:mail_proto --verbose_failures`。
- 补充结果：`INFO: Found 3 targets...`，`INFO: Build completed successfully, 458 total actions`，耗时 `85.779s`。
- descriptor 产物：`bazel-bin/proto/paging_proto-descriptor-set.proto.bin` 187 bytes，`bazel-bin/proto/message_proto-descriptor-set.proto.bin` 780 bytes，`bazel-bin/proto/mail_proto-descriptor-set.proto.bin` 650 bytes；`feed_proto` 聚合目标 descriptor 为 0 bytes，具体 proto 目标 descriptor 均非空。
- 非阻断警告：Bazel 运行时出现 hostname 非 ASCII 导致的 Java log handler 警告、Go 版本列表联网超时警告、Visual Studio `/showIncludes` 语言识别警告；这些均未阻断 proto 目标构建。

### SDK-RT-007

- 测试入口：新增 `app/src/test/kotlin/com/bytetrain/feishuclone/features/message/data/SdkMessageRepositoryTest.kt`，并在 app Gradle 配置中新增 `testImplementation(kotlin("test"))`。
- 覆盖第一页：`sdkBackedMessageRepositoryReturnsFirstPageWithMappedFields` 通过 `SdkMessageRepository(RuntimeMessageSdkClient(totalCount = 45), fallback)` 请求 `pageSize = 20, cursor = null`，断言 20 条、`nextCursor = "20"`、`hasMore = true`。
- 覆盖关键字段映射：同一第一页用例断言首条领域模型字段，包括 `id = "message-1"`、`conversationName = "Calendar Bot 1"`、`conversationType = BOT`、`avatarUrl = null`、`avatarText = "C"`、`lastMessagePreview`、`lastMessageTimeMillis`、`unreadCount`、`isPinned`、`isMuted`、`isBot`。
- 覆盖下一页：`sdkBackedMessageRepositoryUsesNextCursorForNextPage` 使用第一页返回的 cursor 请求下一页，断言首尾 `message-21` / `message-40`、`nextCursor = "40"`、`hasMore = true`。
- 覆盖非法 cursor fallback：`sdkBackedMessageRepositoryDelegatesInvalidCursorToFallback` 使用 `cursor = "not-a-number"`，断言 repository 委托 fallback，且 fallback 收到原始 `pageSize` / `cursor` 并返回 fallback page。
- 验证命令：`.\\gradlew.bat :app:testDebugUnitTest` 成功，输出 `BUILD SUCCESSFUL in 14s`；测试结果 XML 显示 `tests="3" skipped="0" failures="0" errors="0"`。

### SDK-RT-008

- 测试入口：新增 `app/src/test/kotlin/com/bytetrain/feishuclone/features/mail/data/SdkMailRepositoryTest.kt`，复用 `SDK-RT-007` 引入的 app local unit test 配置。
- 覆盖第一页：`sdkBackedMailRepositoryReturnsFirstPageWithMappedFields` 通过 `SdkMailRepository(RuntimeMailSdkClient(totalCount = 45), fallback)` 请求 `pageSize = 20, cursor = null`，断言 20 条、`nextCursor = "20"`、`hasMore = true`。
- 覆盖关键字段映射：同一第一页用例断言首条领域模型字段，包括 `id = "mail-1"`、`sender = "Feishu Updates"`、`subject = "Weekly product digest #1"`、`preview`、`timestampMillis`、`unread = true`、`attachmentCount = 0`、`mailType = UPDATE`、`actionText = null`。
- 覆盖下一页：`sdkBackedMailRepositoryUsesNextCursorForNextPage` 使用第一页返回的 cursor 请求下一页，断言首尾 `mail-21` / `mail-40`、`nextCursor = "40"`、`hasMore = true`。
- 覆盖非法 cursor fallback：`sdkBackedMailRepositoryDelegatesInvalidCursorToFallback` 使用 `cursor = "not-a-number"`，断言 repository 委托 fallback，且 fallback 收到原始 `pageSize` / `cursor` 并返回 fallback page。
- 验证命令：`.\\gradlew.bat :app:testDebugUnitTest` 成功，输出 `BUILD SUCCESSFUL in 2s`；邮箱测试结果 XML 显示 `tests="3" skipped="0" failures="0" errors="0"`，消息和邮箱两个 test suite 合计 6 个用例均通过。

### SDK-RT-009

- 验证命令：`cargo test --manifest-path sdk/rust/Cargo.toml`。
- 权限说明：首次在沙箱内运行时因需要写入 `sdk/rust/target/debug/.cargo-build-lock` 被拒绝访问；按权限流程获权后重试同一命令通过。
- 结果摘要：Rust unit tests 运行 8 个，`8 passed; 0 failed; 0 ignored; 0 measured; 0 filtered out; finished in 0.00s`。
- 覆盖用例：`message_first_page_uses_empty_cursor`、`mail_middle_page_uses_cursor_as_start_index`、`message_last_page_has_no_next_cursor`、`empty_cursor_is_equivalent_to_first_page`、`invalid_cursor_returns_structured_error`、`out_of_range_cursor_returns_structured_error`、`page_size_accepts_min_and_max_boundaries`、`page_size_rejects_values_outside_boundaries`。
- Doc-tests：`0 passed; 0 failed; 0 ignored; 0 measured; 0 filtered out`。

### SDK-RT-010

- Bazel 入口：继续使用此前验证可用的本地二进制 `C:\Users\23064\.bazelisk\downloads\sha256\2db62663b47eb90143932cd553f66840dc03da8e7ce0a23a1302e63fdc234254\bin\bazel.exe`；避免 `bazelisk` 因无 `.bazelversion` 再次联网解析 Bazel `latest`。
- 聚合尝试：`bazel.exe --batch build //app:app //app:app_lib //features/message:data //features/mail:data //proto:feed_proto //proto:paging_proto //proto:message_proto //proto:mail_proto //sdk/rust:bytetrain_feed_sdk --verbose_failures` 在工具层 300s 超时且未返回 Bazel 日志；检查到残留 `bazel` 进程 PID 24512，普通权限停止失败，获权执行 `Stop-Process -Id 24512 -Force` 后清理。
- Proto build 命令：`bazel.exe --batch build //proto:feed_proto //proto:paging_proto //proto:message_proto //proto:mail_proto --verbose_failures`。
- Proto build 结果：`INFO: Found 4 targets...`，`INFO: Build completed successfully, 1 total action`，耗时 `12.989s`。
- SDK Bazel test 命令：`bazel.exe --batch test //sdk/rust:bytetrain_feed_sdk_test --verbose_failures --test_output=errors`。
- SDK Bazel test 结果：`//sdk/rust:bytetrain_feed_sdk_test PASSED in 0.3s`，`Executed 1 out of 1 test: 1 test passes`，整体构建 `INFO: Build completed successfully, 141 total actions`，耗时 `65.910s`。
- Feature data build 命令：`bazel.exe --batch build //features/message:data //features/mail:data --verbose_failures`。
- Feature data build 结果：`INFO: Found 2 targets...`，`INFO: Build completed successfully, 1 total action`，耗时 `6.318s`。
- App library build 命令：`bazel.exe --batch build //app:app_lib --verbose_failures`。
- App library build 结果：`Target //app:app_lib up-to-date: bazel-bin/app/libapp_lib.jar`，`INFO: Build completed successfully, 258 total actions`，耗时 `282.712s`。
- App binary 首次命令：`bazel.exe --batch build //app:app --verbose_failures`。
- App binary 首次结果：分析成功但构建失败于 `Linking Android Resources in app/app_lib_files/library.ap_`；根因是 action JVM 使用 `-Xms3G -Xmx3G` 时 Windows 返回 `Native memory allocation (mmap) failed to map 3221225472 bytes` / `页面文件太小，无法完成操作。`。
- App binary 重试命令：`bazel.exe --batch build //app:app --verbose_failures --jobs=1 --local_cpu_resources=1`。
- App binary 重试结果：低并发重试成功，产物包括 `bazel-bin/app/app_deploy.jar`、`bazel-bin/app/app_unsigned.apk`、`bazel-bin/app/app.apk`；输出 `INFO: Build completed successfully, 25 total actions`，耗时 `147.924s`。
- Bazel test 目标确认命令：`bazel.exe --batch query "kind('.*_test rule', //...)"`。
- Bazel test 目标确认结果：当前仓库仅返回 `//sdk/rust:bytetrain_feed_sdk_test`，app/proto 当前无额外 Bazel test rule；SDK test 已在本任务执行通过。
- 非阻断警告：多次 Bazel 运行仍出现 hostname 非 ASCII 导致的 Java `SimpleLogHandler` 加载警告；Android/app 构建过程中出现 MSVC `C4819` 代码页警告、`LINK : warning LNK4001`、`--local_cpu_resources` deprecated 警告。上述警告未阻断最终 proto、feature、app library、app binary 和 SDK test 结果。

### SDK-RT-011

- 文档更新：已更新 `docs/ai-context/sdk/sdk-adapter-evidence.md`，从早期 `SDK-006` adapter boundary 说明扩展为 `connect-sdk-protobuf-runtime` 最终运行时证据。
- 最终运行时路径：文档记录 `MainActivity -> AppRepositoryProvider -> SdkMessageRepository(RuntimeMessageSdkClient(), MockMessageRepository())` 和 `MainActivity -> AppRepositoryProvider -> SdkMailRepository(RuntimeMailSdkClient(), MockMailRepository())`。
- UI 边界：文档明确 UI 仍通过 `MessageRepository` / `MailRepository` 的 `loadPage(pageSize, cursor)` 使用数据，不导入 SDK DTO、protobuf DTO、Rust 类型或 runtime bridge 实现细节。
- Provider 行为：文档记录默认 `sdkRuntimeEnabled = true`，SDK-backed repository 构造失败时返回 mock fallback，SDK 加载失败时由 SDK repository 委托 fallback 并保留原始 `pageSize` / `cursor`。
- SDK client 语义：文档记录 `RuntimeMessageSdkClient` / `RuntimeMailSdkClient` 的 `pageSize in 1..200`、空 cursor 首页、非空 cursor 零基 start index、非法 cursor/page size 失败、`nextCursor` 仅在 `hasMore` 为 true 时返回。
- Protobuf 形态：文档记录当前未接入 generated Kotlin protobuf bindings，运行时通过 `PageRequest`、`PageInfo`、`MessagePageResponse`、`MailPageResponse` 的 documented shape mapping 对齐；mail Rust-only 字段默认 `attachmentCount = 0`、`mailType = UPDATE`、`actionText = null`。
- 验证证据：文档汇总 Gradle focused Kotlin tests、`cargo test --manifest-path sdk/rust/Cargo.toml`、Bazel proto / SDK test / feature data / app library / app binary build 证据。
- 剩余限制：文档记录尚未接入 native Rust FFI、尚未接入 generated Kotlin protobuf bindings、runtime client 仍使用 deterministic local data、mock fallback 仍保留、Windows Bazel 需使用本地 Bazel cache 二进制、`//app:app` 在本机需低并发重试以及既有非阻断警告。

### SDK-RT-012

- Tasks 更新：已将 `SDK-RT-011` 和 `SDK-RT-012` 勾选为完成。
- 输出摘要：本 `tasks.md` 已保留 `SDK-RT-010` 的最终 Bazel 命令和结果，包括 proto build 成功、SDK Bazel test 1/1 passed、feature data build 成功、app library build 成功、app binary 默认并发内存失败后低并发重试成功，以及 Bazel test query 仅返回 `//sdk/rust:bytetrain_feed_sdk_test`。
- 文档证据：本 `tasks.md` 已补充 `SDK-RT-011` 文档更新范围、最终 runtime path、fallback 行为、protobuf-shaped mapping、验证证据和剩余限制。
- 完成状态：`connect-sdk-protobuf-runtime` 的 12 个任务均已标记完成，后续可进入 OpenSpec archive / final evidence 收尾流程。
