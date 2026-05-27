# 模块边界

## App

App 模块只负责应用启动和应用级导航。

## 功能模块

功能模块负责单个产品区域内的领域模型、功能状态、数据适配和 UI。

## Shared

Shared 模块不能依赖具体 feature 模块。

## Proto

Proto 文件定义稳定的跨语言数据契约。

## Rust SDK

Rust SDK 负责数据解析、异步服务边界，以及后续面向 FFI 的逻辑。
