package com.grean.dustctrl;

/**
 * Created by weifeng on 2017/9/8.
 */

public interface ReadWriteConfig {
    public void saveConfig(String key,long data);
    public void saveConfig(String key,int data);
    public void saveConfig(String key,float data);
    public void saveConfig(String key,String data);
    public void saveConfig(String key,boolean data);
    public boolean getConfigBoolean(String key);
    public float getConfigFloat(String key);
    public String getConfigString(String key);
    public long getConfigLong(String key);
    public int getConfigInt(String key);
}
