# 构建命令

当前仓库已经有 Bazel 占位文件，但还没有声明 Android、Kotlin、Rust 或 proto 工具链。

规划中的命令：

```bash
bazel build //...
bazel test //...
bazel query //...
bazel clean
```

在 Bazel 工具链接入之前，这些命令只是设计目标，还不是已经验证通过的命令。
