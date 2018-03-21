package com.gzplanet.xposed.sonystatusactivityfix;

import android.content.res.XModuleResources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SonyStatusActivityFix implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources  {
    final static String PKGNAME_SYSTEMUI = "com.android.systemui";
    final static String CLASSNAME_SIGNALCLUSTERVIEW = "com.android.systemui.statusbar.SignalClusterView";
    final static String CLASSNAME_PHONESTATE = "com.android.systemui.statusbar.SignalClusterView.PhoneState";

    static String MODULE_PATH;
    static XModuleResources mModResource;
    static Drawable mActivityUp;
    static Drawable mActivityDown;
    static Drawable mActivityUpGrey;
    static Drawable mActivityDownGrey;

    @Override
    public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
    }

    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(PKGNAME_SYSTEMUI))
            return;

        // module resources
        mModResource = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        mActivityUpGrey = mModResource.getDrawable(R.drawable.ic_activity_up_grey, null);
        mActivityDownGrey = mModResource.getDrawable(R.drawable.ic_activity_down_grey, null);
        mActivityUp = mModResource.getDrawable(R.drawable.ic_activity_up, null);
        mActivityDown = mModResource.getDrawable(R.drawable.ic_activity_down, null);
    }

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

                        setInOutView(in, out);
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

                            setInOutView(in, out);
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

    private void setInOutView(ImageView in, ImageView out) {
        if (in != null && out != null) {
            if (in.getVisibility() == View.GONE) {
                in.setVisibility(View.VISIBLE);
                in.setImageDrawable(mActivityDownGrey);
            } else
                in.setImageDrawable(mActivityDown);

            if (out.getVisibility() == View.GONE) {
                out.setVisibility(View.VISIBLE);
                out.setImageDrawable(mActivityUpGrey);
            } else
                out.setImageDrawable(mActivityUp);
        }
    }
}
