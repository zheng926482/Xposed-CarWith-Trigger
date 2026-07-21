# 保留 Xposed Hook 类不被混淆
-keep class com.carwith.play.CarWithHook { *; }
-keep class de.robv.android.xposed.** { *; }
-keep class de.robv.android.xposed.IXposedHookLoadPackage { *; }
