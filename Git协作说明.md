推荐的流程是：

```text
本地开发功能
→ 推送到自己的功能分支
→ 提交 Pull Request
→ 关联 OpenSpec change
→ 代码 Review
→ Bazel build / test 通过
→ 合并到 main
```

也就是说，开发好的功能**应该通过 PR 提交到 GitHub 仓库，再合并进 main 分支**。

---

# 推荐的 Git 协作流程

假设你要开发“消息列表分页功能”。

## 1. 从 main 拉新分支

```bash
git checkout main
git pull origin main
git checkout -b feature/message-pagination
```

分支名可以统一成：

```text
feature/message-tab
feature/mail-tab
feature/shared-paging
feature/rust-sdk
feature/proto-contract
feature/bazel-build
docs/openspec-init
fix/bazel-proto-deps
```

---

## 2. 先写 OpenSpec change

比如：

```text
openspec/changes/add-message-pagination/
├── proposal.md
├── design.md
├── tasks.md
└── specs/
```

这一步对应文档里的要求：每个阶段性变更都先提交 OpenSpec change，包括 `proposal.md`、`design.md`、`tasks.md` 和 delta specs。

---

## 3. 本地实现功能

然后再写代码，例如：

```text
features/message/ui/MessageListScreen.kt
features/message/ui/MessageItemCell.kt
features/message/MessageViewModel.kt
features/message/data/MockMessageRepository.kt
```

同时更新：

```text
features/message/BUILD.bazel
openspec/changes/add-message-pagination/tasks.md
docs/ai-context/xxx.md
```

---

## 4. 本地跑构建和测试

至少要跑：

```bash
bazel build //...
bazel test //...
```

如果只改了某个模块，也可以先跑局部 target：

```bash
bazel build //features/message:message_ui
bazel test //features/message:message_test
```

文档要求项目能运行 `bazel build`、`bazel run`、`bazel test`，并且要记录构建和测试结果。

---

## 5. 提交 commit

```bash
git add .
git commit -m "feat(message): add paged message list"
git push origin feature/message-pagination
```

commit 信息建议规范一点：

```text
feat(message): add message list
feat(mail): add mail tab
feat(shared): add paging state abstraction
feat(proto): add message and mail contracts
feat(bazel): add message module targets
docs(openspec): add message pagination change
fix(bazel): fix proto visibility
```

---

## 6. 创建 PR

PR 的目标分支是：

```text
main
```

PR 里要写清楚：

```md
## What

实现消息列表分页功能。

## Related OpenSpec Change

openspec/changes/add-message-pagination

## AI Usage

- 使用 AI 生成 UI 状态矩阵初稿
- 使用 AI 枚举分页边界 case
- 使用 AI 辅助分析 Bazel BUILD 依赖

## Human Decisions

- 采用 LazyColumn 实现长列表
- 暂时使用 mock 数据，不接真实 Rust SDK
- 分页大小根据屏幕可见数量估算

## Test

- bazel build //features/message:message_ui passed
- bazel test //features/message:message_test passed

## Evidence

- tasks.md 已更新
- OpenSpec validate 已通过
```

这样导师或队友看 PR 时，就能同时看到：

```text
需求
设计
代码
测试
AI 使用记录
人工决策
构建结果
```

这正好符合文档强调的“证据链”。

---

# 一个功能的标准合并条件

建议你们团队规定：PR 只有满足这些条件才能 merge 到 main。

```text
1. 有对应 OpenSpec change
2. proposal.md / design.md / tasks.md 已写
3. tasks.md 已更新完成状态
4. 代码实现和 spec 一致
5. Bazel build 通过
6. Bazel test 通过
7. 至少一个队友 Review
8. 没有明显破坏模块边界
9. AI 使用过程有记录
10. PR 描述完整
```

---

# main 分支应该保持什么状态？

`main` 分支应该永远尽量保持：

```text
可构建
可运行
可测试
文档和代码一致
```

所以 main 分支不应该放半成品功能。

如果一个功能还没做完，应该留在功能分支里。

如果功能太大，可以拆成多个小 PR：

```text
PR 1：添加 OpenSpec change 和数据模型
PR 2：实现消息列表 UI
PR 3：实现分页状态
PR 4：实现详情跳转
PR 5：补 Bazel target 和测试
```

这样比一个超大 PR 更适合团队协作。

---

# Git 提交约束和自动检查

为了把上面的协作流程真正落到本地开发过程里，本项目使用 **Git Hooks + 仓库内脚本** 做提交约束。

也就是说，开发者在执行 `git commit` 或 `git push` 时，会自动触发对应检查：

```text
git commit
→ pre-commit：检查分支、暂存文件、OpenSpec 关联
→ commit-msg：检查 commit message 格式

git push
→ pre-push：执行 bazel build //...
→ pre-push：执行 bazel test //...
```

## 1. 启用 Git Hooks

Git 默认只会读取本机 `.git/hooks/` 目录，但这个目录不会提交到仓库里。

所以本项目把 hooks 放在仓库内：

```text
.githooks/
├── pre-commit
├── commit-msg
└── pre-push

scripts/
├── setup-git-hooks.ps1
├── setup-git-hooks.sh
└── git-hooks/
    ├── check_pre_commit.py
    ├── check_commit_msg.py
    └── check_pre_push.py
```

首次拉取仓库后，需要在仓库根目录执行一次：

```bash
# Windows PowerShell
./scripts/setup-git-hooks.ps1
```

或：

```bash
# macOS / Linux / Git Bash
./scripts/setup-git-hooks.sh
```

脚本实际执行的是：

```bash
git config core.hooksPath .githooks
```

执行后，本仓库的 Git 操作就会使用 `.githooks/` 里的检查逻辑。

## 2. commit message 规范

本项目要求 commit message 使用下面格式：

```text
<type>(<scope>): <subject>
```

允许的 `type`：

```text
feat
fix
docs
test
refactor
chore
build
ci
```

允许的 `scope`：

```text
app
message
mail
shared
proto
bazel
openspec
docs
sdk
```

示例：

```text
feat(message): add paged message list
feat(mail): add mail tab
feat(shared): add paging state abstraction
feat(proto): add message and mail contracts
feat(bazel): add message module targets
docs(openspec): add message pagination change
fix(bazel): fix proto visibility
```

不符合格式的 commit 会被 `commit-msg` hook 拦截。

## 3. pre-commit 检查内容

`pre-commit` 会在提交创建前执行轻量检查：

```text
1. 禁止直接在 main / master 分支提交
2. 禁止提交 .log / .tmp / .bak / .swp / .DS_Store / Thumbs.db 等临时文件
3. 如果修改 app / features / shared / proto / sdk 下的源码，需要同时提交 openspec/changes/ 下的变更说明
```

如果确实是紧急情况，可以临时绕过对应检查：

```bash
# 允许在 main/master 上提交一次
BYTETRAIN_ALLOW_MAIN_COMMIT=1 git commit -m "fix(bazel): repair emergency build"

# 跳过 OpenSpec 关联检查
BYTETRAIN_SKIP_OPENSPEC_CHECK=1 git commit -m "chore(docs): fix typo"
```

绕过检查只适合紧急情况，正常协作仍然应该通过功能分支和 OpenSpec change 完成。

## 4. pre-push 检查内容

`pre-push` 会在推送到远端前执行较重的质量检查：

```bash
bazel build //...
bazel test //...
```

如果本机没有安装 `bazel` 或 `bazelisk`，推送会被拦截。

紧急情况下可以临时跳过：

```bash
BYTETRAIN_SKIP_PRE_PUSH=1 git push origin feature/message-pagination
```

但跳过后，PR 描述里需要说明原因，并在后续补充构建和测试结果。

## 5. 为什么这样分层

不建议在每次 `git commit` 时都运行完整的 `bazel build //...` 和 `bazel test //...`，因为这会让提交过程变慢。

所以本项目采用分层策略：

```text
commit 阶段：检查规范和证据链
push 阶段：检查构建和测试
PR 阶段：通过 Review 和 CI 再次确认
```

这样既能保持本地开发体验，也能保证进入远端仓库的代码尽量满足：

```text
有规范提交
有关联 OpenSpec
可构建
可测试
可 Review
```

---
