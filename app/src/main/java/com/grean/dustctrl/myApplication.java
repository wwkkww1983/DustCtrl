package com.grean.dustctrl;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.StringBuilderPrinter;

/**
 * Created by Administrator on 2017/8/24.
 */

public class myApplication extends Application {
    private static  final String tag = "myApplication";
    private static myApplication instance;

    public myApplication(){
        CtrlCommunication.getInstance();//.setDustParaK(getConfigFloat("DustParaK"));
        NoiseCommunication.getInstance();
        Log.d(tag,"开机");
    }

    public static myApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.instance = this;
        loadConfig();
        Log.d(tag,"start");
    }

    private void loadConfig(){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(!sp.getBoolean("FactorySetting",false)){
            editor.putBoolean("FactorySetting",true);
            editor.putFloat("DustParaK",1f);
            editor.putInt("MotorRounds",10);
            editor.putInt("MotorTime",100);
            editor.putLong("AutoCalTime",1483200000000l);
            editor.putLong("AutoCalInterval",86400000l);
            editor.putBoolean("AutoCalibrationEnable",true);
            editor.commit();
        }

    }

    public void saveConfig(String key,long data){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key,data);
        editor.commit();
    }

    public void saveConfig(String key,int data){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key,data);
        editor.commit();
    }

    public void saveConfig(String key,boolean data){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key,data);
        editor.commit();
    }

    public boolean getConfigBoolean(String key){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        return sp.getBoolean(key,false);
    }

    public float getConfigFloat(String key){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        return sp.getFloat(key,0f);
    }

    public int getConfigInt(String key){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        return sp.getInt(key,10);
    }

    public long getConfigLong(String key){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        return sp.getLong(key,10);
    }
}
