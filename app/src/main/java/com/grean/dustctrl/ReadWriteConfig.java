package com.grean.dustctrl;

import com.grean.dustctrl.device.DevicesManage;

/**
 * Created by weifeng on 2017/9/8.
 */

public interface ReadWriteConfig {
    void saveConfig(String key,long data);
    void saveConfig(String key,int data);
    void saveConfig(String key,float data);
    void saveConfig(String key,String data);
    void saveConfig(String key,boolean data);
    boolean getConfigBoolean(String key);
    float getConfigFloat(String key);
    String getConfigString(String key);
    long getConfigLong(String key);
    int getConfigInt(String key);
    void saveUploadSetting(String key);
    void saveDeviceSetting(DevicesManage manage);
}
