# Final Retrospective

记录日期：2026-06-05。

## Reusable prompts

- “请先阅读当前 OpenSpec change 的 proposal/design/tasks，再按 tasks 顺序实现并在每项完成后补证据。”
- “请把构建失败按命令、输出摘要、根因、修复方式、重试命令记录到 `docs/ai-context/build-system/common-build-errors.md`。”
- “请用 Bazel query 验证 app、feature、shared 的显式依赖边界，并同步 `docs/project/module-boundaries.md`。”
- “请把 AI 建议拆成采纳、拒绝、暂缓三类，并说明人工决策原因。”
- “请为最终验收生成可重复运行的脚本入口和精简 evidence 文件，不要保存大日志。”

## Effective AI patterns

- 先建立 OpenSpec 任务边界，再做代码或文档改动。
- 对复杂迁移使用分阶段策略：UI 主链路、SDK/runtime、Bazel、IDE、最终证据。
- 使用 focused check scripts 验证窄任务，比一次性大范围构建更容易定位失败。
- 对本机 Bazel 环境失败保持单独记录，避免污染源码质量判断。
- 将 AI 可读上下文沉淀到 `docs/ai-context/`，减少后续助手反复探索成本。

## Accepted suggestions

- OpenSpec-first 和 evidence-first 工作流。
- App/provider/repository 的组合根设计。
- Shared UI model + feature domain model 的边界。
- Bazel final acceptance，包括 app/proto/features/Rust/query。
- Trae/VS Code 构建入口复用同一 `scripts/commands/ide-build.ps1`。
- 最终 `scripts/commands/verify-final-bazel-delivery.ps1` 生成 concise evidence。

## Ineffective or rejected suggestions

- 只用 Gradle 作为最终验收。该建议不满足最终 Bazel 交付要求。
- 在当前阶段接入 native Rust FFI。风险和时间成本高，且 UI runtime 可先通过 SDK client interface 保持边界。
- 在 UI 主链路直接依赖 protobuf/generated DTO。会把 transport contract 泄漏到 UI 层。
- 把大段 Bazel 日志直接复制进文档。最终采用摘要、根因和重试命令。
- 把飞书多维表格作为正式验收证据。最终只作为可选入口链接。

## Build failures diagnosed with AI help

- `//proto:...` 在 Bazel 中被解析为普通 target，已通过兼容 alias 处理。
- 首次 protobuf 工具链构建超时，已通过延长超时和重试记录解决。
- Bzlmod 外部依赖下载超时，已通过 distdir、repository cache、Maven mirror、Go proxy 记录和修复。
- Android app target 规则、Java 17 runtime、manifest package、resource `R` 生成问题已修复并验证。
- Windows 非 ASCII 主机名 Bazel Java 日志异常记录为非阻塞噪声。
- 受限入口执行 `bazel.cmd`/`bazel.exe`/`bazelisk.exe` 返回 `Access is denied`，已记录为环境权限问题，并在允许执行本机 Bazel 后完成验证。

## Remaining limitations

- SDK runtime 当前不是 native Rust FFI，而是 Kotlin runtime bridge，保留后续替换空间。
- Generated Kotlin protobuf bindings 尚未接入 UI runtime 主链路。
- 飞书多维表格最终链接更新属于可选第五阶段，不是本地正式验收证据。
- 本机 Windows Bazel 输出仍可能包含非阻塞日志噪声和环境相关 warning。
- OpenSpec archive 对 doc-only/infrastructure changes 需要使用 `--skip-specs` 判断，避免把无长期 spec delta 的变更误并入产品 spec。
