# Tasks: wire-bazel-build

## 1. 工具链决策

- [ ] BZL-001 确定 Android、Kotlin、proto、Rust 对应 Bazel rules，并记录采用和拒绝原因。

## 2. Targets

- [ ] BZL-002 新增 proto Bazel targets，并验证 `bazel build //proto:...`。
- [ ] BZL-003 新增 shared、message、mail 模块的 Kotlin targets。
- [ ] BZL-004 新增 Android app target，并验证 App 构建 target。
- [ ] BZL-005 新增 Rust SDK Bazel target；可行时通过 Bazel 验证 Rust 测试。

## 3. Query 与可观测性

- [ ] BZL-006 运行 Bazel query 检查 App 和模块依赖，并记录摘要。
- [ ] 在 `docs/ai-context/build-commands.md` 中记录构建命令。
- [ ] 在 `docs/ai-context/module-boundaries.md` 中记录依赖边界。
- [ ] 在 `docs/ai-context/common-build-errors.md` 中记录真实构建失败和修复方式。

## 4. 证据

- [ ] 在本 `tasks.md` 中记录成功的 build/test 命令输出或摘要。
- [ ] 更新飞书多维表格中 Bazel 任务的证据链接。
