# ByteTrain Android 开发技能

日期：2026-06-09

本地技能路径：

```text
C:\Users\24155\.codex\skills\bytetrain-android-development
```

## 目的

此技能记录了本项目可复用的 Android 开发工作流，供后续 Codex 会话在需要修改或验证以下内容时使用：

- `MainActivity` 和应用层组合逻辑。
- 消息和邮件功能的 UI、仓库、映射器以及领域模型。
- 共享 Android 列表/详情 UI 基础组件。
- Android Gradle 和 Bazel 验证。
- OpenSpec Android 任务证据。

## 触发示例

```text
使用 $bytetrain-android-development 为消息列表添加 Android UI 状态。
使用 $bytetrain-android-development 验证邮件详情屏幕变更。
使用 $bytetrain-android-development 在 UI 变更后更新 Android OpenSpec 证据。
```

## 随附参考资料

- `references/android-architecture.md`：模块归属、依赖规则、常见实现顺序以及 Bazel 目标。
- `references/android-verification.md`：聚焦检查脚本、Gradle/Bazel 命令以及证据表述。
- `references/android-openspec.md`：OpenSpec 工作流、验证命令以及归档就绪检查。

## 验证

此技能已通过以下命令验证：

```powershell
python quick_validate.py C:\Users\24155\.codex\skills\bytetrain-android-development
```

结果：通过。本地 Python 运行环境需要 `PyYAML`；该依赖仅为验证目的安装到了临时目录中。
