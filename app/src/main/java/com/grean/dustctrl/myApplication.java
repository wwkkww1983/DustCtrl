package com.grean.dustctrl;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.StringBuilderPrinter;

import com.grean.dustctrl.protocol.GeneralClientProtocol;
import com.grean.dustctrl.protocol.GeneralServerProtocol;
import com.grean.dustctrl.protocol.GetProtocols;
import com.grean.dustctrl.protocol.InformationProtocol;
import com.grean.dustctrl.protocol.TcpClient;
import com.grean.dustctrl.protocol.TcpServer;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by Administrator on 2017/8/24.
 */

public class myApplication extends Application implements ReadWriteConfig{
    private static  final String tag = "myApplication";
    private static myApplication instance;
    private HashMap<String,Object> config = new HashMap<String,Object>();

    public myApplication(){
        CtrlCommunication.getInstance();//.setDustParaK(getConfigFloat("DustParaK"));
        NoiseCommunication.getInstance();
        Log.d(tag,"开机");
    }



    public HashMap<String, Object> getConfig() {
        return config;
    }

    public void setConfig(HashMap<String, Object> config) {
        this.config = config;
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
            editor.putString("ServerIp","192.168.168.134");
            editor.putInt("ServerPort",12803);
            editor.commit();
        }

    }

    public void saveConfig(String key,float data){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putFloat(key,data);
        editor.commit();
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

    public void saveConfig(String key,String data){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key,data);
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

    public String getConfigString(String key){
        SharedPreferences sp = getSharedPreferences("config",MODE_PRIVATE);
        return sp.getString(key," ");
    }
}
