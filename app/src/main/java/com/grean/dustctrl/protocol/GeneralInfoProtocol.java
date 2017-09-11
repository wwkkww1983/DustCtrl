package com.grean.dustctrl.protocol;

import android.content.Context;

import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.process.SensorData;

/**
 * 一般系统系统数据、状态、设置查询
 * Created by weifeng on 2017/9/7.
 */

public interface GeneralInfoProtocol {
    /**
     * 获取当前系统状态，校准状态
     * @return
     */
    String getSystemState();

    /**
     * 获取事实数据
     * @return
     */
    SensorData getSensorData();

    /**
     * 更新实时数据
     * @param data
     */
    void notifySenorData(SensorData data);

    /**
     * 更新实时状态
     * @param string
     */
    void notifySystemState(String string);

    /**
     * 装载设置
     * @param config
     */
    void loadSetting(ReadWriteConfig config);

    void setContext(Context context);

    void setServer(String ip ,int port);

    boolean setAutoCal(boolean enable,long date,long interval);

    void calDust(float target);

    void calDustMeter();

    void setDustMeterResult(boolean bg,boolean span);

    boolean getDustMeterBg();
    boolean getDustMeterSpan();
    int getDustMeterPumpTime();
    int getDustMeterLaserTime();

    float getParaK();
    String getServerIp();
    int getServerPort();
    boolean getAutoCalEnable();
    long getAutoCalTime();
    long getAutoCalInterval();
}
