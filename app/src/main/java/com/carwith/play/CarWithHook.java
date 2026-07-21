package com.carwith.play;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class CarWithHook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "com.miui.carwith";
    private static final String SALT_PLAYER_PACKAGE = "com.salt.music";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE.equals(lpparam.packageName)) {
            return;
        }

        XposedBridge.log("[CarWithPlay] Hook loaded for: " + lpparam.packageName);

        try {
            Class<?> dialogClass = XposedHelpers.findClass("android.app.Dialog", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(dialogClass, "show", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    handlePopupEvent();
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] Dialog show hook failed: " + t);
        }

        try {
            Class<?> activityClass = XposedHelpers.findClass("com.miui.carwith.CarWithActivity", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(activityClass, "onResume", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    handlePopupEvent();
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] Activity hook failed: " + t);
        }

        try {
            XposedHelpers.findAndHookMethod(
                    "android.content.ContextWrapper",
                    lpparam.classLoader,
                    "sendBroadcast",
                    Intent.class,
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Intent intent = (Intent) param.args[0];
                            if (intent != null) {
                                String action = intent.getAction();
                                if ("com.miui.carwith.CONNECTION_SUCCESS".equals(action)) {
                                    handleConnected();
                                } else if ("com.miui.carwith.CONNECTION_DISCONNECTED".equals(action)) {
                                    handleDisconnected();
                                }
                            }
                        }
                    });
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] Broadcast hook failed: " + t);
        }
    }

    private void handlePopupEvent() {
        enableBluetooth();
        enableHighAccuracyLocation();
    }

    private void handleConnected() {
        launchSaltPlayer();
    }

    private void handleDisconnected() {
        disableHighAccuracyLocation();
        stopSaltPlayer();
    }

    private void enableBluetooth() {
        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && !adapter.isEnabled()) {
                adapter.enable();
            }
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] enableBluetooth failed: " + t);
        }
    }

    private Context getCurrentContext() {
        return null;
    }

    private void enableHighAccuracyLocation() {
        try {
            Context context = getCurrentContext();
            if (context == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Settings.Secure.putInt(
                        context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_HIGH_ACCURACY);
            }
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] enableHighAccuracyLocation failed: " + t);
        }
    }

    private void disableHighAccuracyLocation() {
        try {
            Context context = getCurrentContext();
            if (context == null) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Settings.Secure.putInt(
                        context.getContentResolver(),
                        Settings.Secure.LOCATION_MODE,
                        Settings.Secure.LOCATION_MODE_OFF);
            }
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] disableHighAccuracyLocation failed: " + t);
        }
    }

    private void launchSaltPlayer() {
        try {
            Context context = getCurrentContext();
            if (context == null) {
                return;
            }
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(SALT_PLAYER_PACKAGE);
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] launchSaltPlayer failed: " + t);
        }
    }

    private void stopSaltPlayer() {
        try {
            Context context = getCurrentContext();
            if (context == null) {
                return;
            }
            Intent intent = new Intent();
            intent.setAction("com.salt.music.ACTION_STOP");
            context.sendBroadcast(intent);
        } catch (Throwable t) {
            XposedBridge.log("[CarWithPlay] stopSaltPlayer failed: " + t);
        }
    }
}
