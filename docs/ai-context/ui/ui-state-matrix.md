# UI 状态矩阵

本文档记录 `add-ui-main-flow` 第一阶段共享分页 UI 状态。当前阶段先通过 Gradle 验证 Kotlin/Android 编译，后续真实 UI 页面和 Bazel target 接入分别由消息/邮箱任务与 `wire-bazel-build` 跟踪。

## PagingUiState

| 状态 | 数据条件 | UI 预期 | 恢复/下一步 |
| --- | --- | --- | --- |
| `Loading` | 首屏请求开始，尚无可展示数据 | 展示首屏加载态，不展示空列表 | 请求成功后进入 `Content` 或 `Empty`，失败后进入 `Error` |
| `Empty` | 首屏请求成功，返回空列表 | 展示空态，不展示加载更多入口 | 用户可刷新或等待后续数据源更新 |
| `Error` | 首屏请求失败，尚无可展示数据 | 展示首屏错误态和重试入口 | 点击重试后回到 `Loading` |
| `Content` | 已有列表数据，当前未加载更多 | 展示列表内容；当 `hasMore = true` 时允许触发加载更多 | 触发加载更多后进入 `LoadingMore`；无更多数据时保持列表稳定 |
| `LoadingMore` | 已有列表数据，正在请求下一页 | 保留已加载 `items`，在列表尾部展示加载更多状态 | 请求成功后进入新的 `Content`；失败后进入 `LoadMoreError` |
| `LoadMoreError` | 已有列表数据，下一页请求失败 | 保留已加载 `items`，在列表尾部展示加载更多失败和重试入口 | 点击重试后回到 `LoadingMore` |

## 验证口径

- `shared/list/PagingModels.kt` 必须声明以上六种状态。
- `LoadingMore` 必须携带当前已加载的 `items`。
- `LoadMoreError` 必须携带当前已加载的 `items` 和错误 `message`。
- `.\gradlew.bat :app:assembleDebug` 必须能够编译当前 `app`、`shared` 和 `features` 源码。
- Bazel 只保留源码清单和边界说明，真实 Bazel rules 不在本 change 中完成。
