# CarWith Play

Xposed 模块：小米 CarWith 增强工具，包名 com.carwith.play。

- 弹窗时自动开启蓝牙和 Root 高精度定位
- 连接成功后自动播放 SaltPlayer（媒体按钮广播）
- 断开后自动关闭定位并结束 SaltPlayer
- 保留 CarWith 原生广播和自动重连

## 编译
GitHub Actions 自动编译，推送即可。

## 使用
在 LSPosed 激活，作用域勾选 com.miui.carwith。

## 依赖
- Xposed API 82
- Root（用于定位开关）
- SaltPlayer (com.salt.music)
