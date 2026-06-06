# 任务：init-project-architecture

- [x] 创建 app、feature、shared、proto、SDK、OpenSpec 和 AI 上下文目录。
  - 证据：本 change 新增的仓库文件。
- [x] 增加根 README 和 Bazel 占位文件。
  - 证据：`README.md`、`MODULE.bazel`、`BUILD.bazel`。
- [x] 增加消息和邮箱领域模型占位。
  - 证据：`features/message/domain`、`features/mail/domain`。
- [x] 增加共享分页和导航占位。
  - 证据：`shared/list/PagingModels.kt`、`shared/navigation/AppRoutes.kt`。
- [x] 增加初始 protobuf 契约占位。
  - 证据：`proto/message.proto`、`proto/mail.proto`、`proto/paging.proto`。
- [x] 增加 Rust SDK 骨架。
  - 证据：`sdk/rust/Cargo.toml`、`sdk/rust/src/lib.rs`。
- [x] 增加 AI 可读取上下文文档。
  - 证据：`docs/project/project-structure.md`、`build-commands.md`、`module-boundaries.md`、`common-build-errors.md`。
- [x] 增加 OpenSpec 项目文件和 Prompt 记录。
  - 证据：`openspec/project.md`、`openspec/prompt.md`、当前 change 目录。
- [x] 验证真实 Bazel 构建。
  - 证据：该初始化阶段遗留验证已由 `verify-final-bazel-delivery` 关闭；`docs/evidence/final-bazel-delivery-evidence.md` 记录 app、proto、shared/feature Kotlin、Rust SDK test 和 app dependency query 均通过；`openspec/changes/verify-final-bazel-delivery/tasks.md` 已记录命令输出摘要。
