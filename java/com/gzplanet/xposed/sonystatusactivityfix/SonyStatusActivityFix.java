package com.gzplanet.xposed.sonystatusactivityfix;

import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SonyStatusActivityFix implements IXposedHookLoadPackage {
    final static String PKGNAME_SYSTEMUI = "com.android.systemui";
    final static String CLASSNAME_SIGNALCLUSTERVIEW = "com.android.systemui.statusbar.SignalClusterView";
    final static String CLASSNAME_PHONESTATE = "com.android.systemui.statusbar.SignalClusterView.PhoneState";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(PKGNAME_SYSTEMUI))
            return;

        try {
            XposedHelpers.findAndHookMethod(CLASSNAME_SIGNALCLUSTERVIEW, lpparam.classLoader, "apply", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    final boolean wifiVisible = XposedHelpers.getBooleanField(param.thisObject, "mWifiVisible");
                    if (wifiVisible) {
                        ImageView in = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mWifiActivityIn");
                        ImageView out = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mWifiActivityOut");

                        if (in != null && out != null)
                            if (in.getVisibility() == View.GONE && out.getVisibility() == View.GONE)
                                in.setVisibility(View.INVISIBLE);
                    }
                }
            });
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("SignalClusterView not found");
            return;
        } catch (NoSuchMethodError e) {
            XposedBridge.log("apply not found");
            return;
        }

        try {
            XposedHelpers.findAndHookMethod(CLASSNAME_PHONESTATE, lpparam.classLoader, "apply", boolean.class,
                    new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    ImageView in = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mMobileActivityIn");
                    ImageView out = (ImageView) XposedHelpers.getObjectField(param.thisObject, "mMobileActivityOut");

                    if (in != null && out != null)
                        if (in.getVisibility() == View.GONE && out.getVisibility() == View.GONE)
                            in.setVisibility(View.INVISIBLE);
                }
            });
        } catch (XposedHelpers.ClassNotFoundError e) {
            XposedBridge.log("SignalClusterView.PhoneState not found");
            return;
        } catch (NoSuchMethodError e) {
            XposedBridge.log("PhoneState.apply not found");
            return;
        }
    }
}
