# 移动端 UI 主链路能力增量

## ADDED Requirements

### Requirement: App MUST 提供两个主 Tab

Android App MUST 提供消息和邮箱两个 Tab，作为核心导航入口。

#### Scenario: 用户切换 Tab

- Given App 已启动
- When 用户选择消息或邮箱 Tab
- Then App 展示对应列表，并保持两个 Tab 的导航契约稳定

### Requirement: 消息 Tab MUST 支持分页 mock 会话

消息 Tab MUST 从包含 10000 条 mock 记录的分页数据源渲染飞书风格会话列表。

#### Scenario: 用户加载更多消息

- Given 消息列表还有更多记录
- When 用户滚动到加载更多阈值
- Then 下一页数据通过 repository 加载并追加到列表

### Requirement: 邮箱 Tab MUST 支持分页 mock 邮件卡片

邮箱 Tab MUST 从包含 10000 条 mock 记录的分页数据源渲染 QQ 邮箱提醒风格邮件卡片。

#### Scenario: 用户加载更多邮件

- Given 邮箱列表还有更多记录
- When 用户滚动到加载更多阈值
- Then 下一页数据通过 repository 加载并追加到列表

### Requirement: 列表和详情 UI MUST 复用共享模型

消息和邮箱 UI MUST 先将业务模型映射为共享列表/详情 UI 模型，再进行渲染。

#### Scenario: 打开详情页

- Given 用户点击消息或邮箱列表项
- When 详情页打开
- Then 页面基于 `DetailModel` 渲染，而不是直接依赖功能专属渲染逻辑

### Requirement: 分页 UI 状态 MUST 显式表达

UI MUST 显式表达加载、空态、错误、内容、加载更多和加载更多失败状态。

#### Scenario: 加载更多失败

- Given 列表已经有内容
- When 下一页加载失败
- Then 现有内容保持可见，并展示可重试的加载更多失败状态
