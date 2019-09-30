package com.grean.dustctrl;

import android.content.Context;
import android.content.SharedPreferences;

import com.grean.dustctrl.UploadingProtocol.UploadingConfigFormat;

import org.json.JSONException;

import static android.content.Context.MODE_PRIVATE;

/**
 * 存储配置文件
 * Created by weifeng on 2018/6/28.
 */

public class SystemConfig implements ReadWriteConfig{
    private static SystemConfig instance;
    private static Context context;

    private SystemConfig(){

    }

    public static SystemConfig getInstance(Context con) {
        if(instance == null){
            context = con;
            instance = new SystemConfig();
        }
        return instance;
    }

    public void loadConfig(){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        if(!sp.getBoolean("FactorySetting",false)){
            editor.putBoolean("FactorySetting",true);
            editor.putInt("MainBoardName",1);
            editor.putInt("DustMeterName",0);
            editor.putFloat("DustParaK",1f);
            editor.putInt("MotorRounds",1600);
            editor.putInt("MotorTime",2000);
            editor.putLong("AutoCalTime",1483200000000l);
            editor.putLong("AutoCalInterval",86400000l);
            editor.putBoolean("AutoCalibrationEnable",true);
            editor.putInt("ClientProtocol",2);

            try {
                editor.putString("UploadConfig", UploadingConfigFormat.getDefaultConfig());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            editor.putInt("ClientProtocolName",0);
            editor.putFloat("AlarmDust",50f);
            editor.commit();
        }

    }



    public void saveConfig(String key,float data){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key,data);
        editor.apply();
    }

    public void saveConfig(String key,long data){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong(key,data);
        editor.apply();
    }

    public void saveConfig(String key,int data){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(key,data);
        editor.apply();
    }

    public void saveConfig(String key,String data){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,data);
        editor.apply();
    }

    public void saveConfig(String key,boolean data){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key,data);
        editor.apply();
    }

    public boolean getConfigBoolean(String key){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        return sp.getBoolean(key,false);
    }

    public float getConfigFloat(String key){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        return sp.getFloat(key,0f);
    }

    public int getConfigInt(String key){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        return sp.getInt(key,0);
    }

    public long getConfigLong(String key){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        return sp.getLong(key,0);
    }

    public String getConfigString(String key){
        SharedPreferences sp = context.getSharedPreferences("config",MODE_PRIVATE);
        return sp.getString(key," ");
    }
}
