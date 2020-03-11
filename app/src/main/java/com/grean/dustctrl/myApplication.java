package com.grean.dustctrl;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;

import java.util.HashMap;

/**
 * Created by Administrator on 2017/8/24.
 */

public class myApplication extends Application {
    private static  final String tag = "myApplication";
    private static myApplication instance;
    private HashMap<String,Object> config = new HashMap<String,Object>();

    public myApplication(){
        Log.d(tag,"开机");
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        init();
    }

    /**
     * 返回当前程序版本名
     */
    public static String getAppVersionName(Context context) {
        String versionName = "";
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            versionName = pi.versionName;
            //versioncode = pi.versionCode;
            if (versionName == null || versionName.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Log.e("VersionInfo", "Exception", e);
        }
        Log.d(tag,"versin Name="+versionName);
        return versionName;
    }

    private void init() {
        SophixManager.getInstance().setContext(this)
                .setAppVersion(getAppVersionName(this))
                .setEnableDebug(true)
                .setPatchLoadStatusStub(new PatchLoadStatusListener() {
                    @Override
                    public void onLoad(final int mode, final int code, final String info, final int handlePatchVersion) {
                        // 补丁加载回调通知
                        if (code == PatchStatus.CODE_LOAD_SUCCESS) {
                            // 表明补丁加载成功
                            Log.i(tag, "onLoad: 成功");
                        } else if (code == PatchStatus.CODE_LOAD_RELAUNCH) {
                            // 表明新补丁生效需要重启. 开发者可提示用户或者强制重启;
                            // 建议: 用户可以监听进入后台事件, 然后应用自杀
                            Log.i(tag, "onLoad: 生效需要重启");
                            restartApp();
                        } else {
                            // 其它错误信息, 查看PatchStatus类说明
                            Log.i(tag, "onLoad: 其它错误信息"+code);
                        }
                        Log.i(tag,"info "+info+";handlePatchVersion"+handlePatchVersion);
                    }
                }).initialize();
        SophixManager.getInstance().queryAndLoadNewPatch();
    }

    public void restartApp(){
        RestartAppTool.restartAPP(this,2000);
    }

    public HashMap<String, Object> getConfig() {
        return config;
    }

    public static myApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
        //loadConfig();

        Log.d(tag,"start");
    }

}
