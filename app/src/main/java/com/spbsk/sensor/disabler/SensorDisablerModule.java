package com.spbsk.sensor.disabler;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SensorDisablerModule implements IXposedHookLoadPackage {
    
    private static final String TAG = "SensorDisabler";
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("android")) {
            return;
        }
        
        XposedBridge.log("[" + TAG + "] Loading for: " + lpparam.packageName);
        
        try {
            Class<?> deviceIdleController = XposedHelpers.findClass(
                "com.android.server.DeviceIdleController", 
                lpparam.classLoader
            );
            
            // Hook 1: registerMotionSensor - блокируем регистрацию
            XposedHelpers.findAndHookMethod(deviceIdleController, "registerMotionSensor", 
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("[" + TAG + "] Blocking registerMotionSensor");
                        param.setResult(false);
                    }
                }
            );
            
            // Hook 2: isMotionActive - всегда false
            XposedHelpers.findAndHookMethod(deviceIdleController, "isMotionActive",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.setResult(false);
                    }
                }
            );
            
            // Hook 3: startMonitoringMotionLocked - пустой метод
            XposedHelpers.findAndHookMethod(deviceIdleController, "startMonitoringMotionLocked",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("[" + TAG + "] Blocking startMonitoringMotionLocked");
                        param.setResult(null);
                    }
                }
            );
            
            // Hook 4: motionLocked - пустой метод
            XposedHelpers.findAndHookMethod(deviceIdleController, "motionLocked",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("[" + TAG + "] Blocking motionLocked");
                        param.setResult(null);
                    }
                }
            );
            
            // Hook 5: Constructor - устанавливаем mUseMotionSensor = false
            XposedHelpers.findAndHookConstructor(deviceIdleController, 
                android.content.Context.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        XposedBridge.log("[" + TAG + "] Setting mUseMotionSensor = false");
                        XposedHelpers.setBooleanField(param.thisObject, "mUseMotionSensor", false);
                    }
                }
            );
            
            XposedBridge.log("[" + TAG + "] All hooks applied successfully");
            
        } catch (Exception e) {
            XposedBridge.log("[" + TAG + "] Error: " + e.getMessage());
        }
    }
}
