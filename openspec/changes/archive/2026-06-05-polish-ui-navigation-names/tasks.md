# 任务：polish-ui-navigation-names

## 1. UI 打磨

- [x] UI-005 将底部导航文字按钮替换为消息/邮箱图标，同时保留无障碍标签。
  - 证据：`MainActivity` 已渲染自定义 `LinearLayout` tabs，内部包含 `ImageView` 图标和紧凑 `TextView` 标签；保留 `contentDescription = label`，并继续基于 `AppRoutes.MESSAGE_LIST` / `AppRoutes.MAIL_LIST` 切换路由。
- [x] UI-007 为底部导航 tabs 增加选中态反馈。
  - 证据：`MainActivity.applyTabSelection` 已更新选中和未选中的图标/文本颜色，并为当前选中 tab 设置浅色选中背景。
- [x] UI-006 移除 mock 消息会话名中的可见数字后缀。
  - 证据：`MockMessageRepository.conversationNameFor` 现在直接返回选中的单聊、群聊或机器人名称；稳定唯一性保留在 `MessageItem.id`。
- [x] UI-008 将消息详情页改为移动端聊天视图，并移除内部调试元信息。
  - 证据：`MessageDetailScreen` 已渲染会话 header、紧凑返回入口、收发聊天气泡和输入栏；不再渲染 `item.detail.metas` 或页面内 `Back to messages` 按钮。
- [x] UI-009 使用 Android 系统返回处理详情页返回，并保留列表滚动位置。
  - 证据：`MainActivity` 已注册 Android 13+ `OnBackInvokedDispatcher` 回调，保留 legacy `onBackPressed` fallback，并通过 `dispatchKeyEvent` 处理模拟器工具栏/键盘 `KEYCODE_BACK`；所有路径都会清空选中的消息/邮箱详情状态。消息和邮箱列表在打开详情时传入当前 `ScrollView.scrollY`，两个列表页都会在重新渲染后恢复 `initialScrollY`。
- [x] UI-010 将邮箱详情页改为移动端邮件阅读视图，并移除内部调试元信息。
  - 证据：`MailDetailScreen` 已渲染紧凑 header 返回入口、主题、发件人行、有效 badges 和邮件正文；不再渲染 `item.detail.metas` 或页面内 `Back to mail` 按钮。
- [x] UI-011 将全宽列表 `Load more` 按钮替换为滚动触发的移动端加载更多行为。
  - 证据：`MessageListScreen` 和 `MailListScreen` 已移除分页 `Button`，改为渲染加载/无更多 footer 文本，并在接近底部时通过 `setOnScrollChangeListener` 触发 `onLoadMore(scrollY)`。`MainActivity` 在展示 loading 状态前记录当前列表滚动位置，通过 `contentContainer.post` 追加下一页，然后在渲染追加内容后恢复之前的位置。

## 2. 验证

- [x] TEST-005 新增并运行聚焦脚本，验证导航图标资源、自定义 tab 渲染、选中态反馈、无障碍标签和 mock 会话名格式。
  - 证据：`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-005.ps1`、`cmd /c openspec validate polish-ui-navigation-names --strict` 和 `.\gradlew.bat :app:assembleDebug` 均通过。
- [x] TEST-006 新增并运行聚焦检查，覆盖聊天式详情页、系统返回导航和滚动位置恢复。
  - 证据：`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-008.ps1`、`powershell -ExecutionPolicy Bypass -File .\scripts\check-msg-006.ps1`、`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-006.ps1`、`cmd /c openspec validate polish-ui-navigation-names --strict` 和 `.\gradlew.bat :app:assembleDebug` 均通过。
- [x] TEST-007 新增并运行聚焦检查，覆盖滚动触发加载更多和追加数据后的滚动位置保持。
  - 证据：`powershell -ExecutionPolicy Bypass -File .\scripts\check-ui-011.ps1`、`powershell -ExecutionPolicy Bypass -File .\scripts\check-msg-005.ps1`、`powershell -ExecutionPolicy Bypass -File .\scripts\check-mail-005.ps1`、`cmd /c openspec validate polish-ui-navigation-names --strict` 和 `.\gradlew.bat :app:assembleDebug` 均通过。
