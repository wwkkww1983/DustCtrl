package com.grean.dustctrl.protocol;

import android.content.Context;

import com.grean.dustctrl.ReadWriteConfig;
import com.grean.dustctrl.process.SensorData;

import java.util.ArrayList;
import java.util.List;

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
    void inquireDustMeterInfo();
    void setDustMeterPumpTime(int pumpTime);
    void setDustMeterLaserTime(int laserTime);
    int getDustMeterPumpTime();
    int getDustMeterLaserTime();

    void setDustCalMeterProcess(int process);
    int getDustMeterCalProcess();

    GeneralHistoryDataFormat getHistoryData(long endDate);

    ArrayList<String> getLog(long dateStart);

    float getParaK();
    String getServerIp();
    int getServerPort();
    String getMnCode();
    void setMnCode(String code);
    boolean getAutoCalEnable();
    long getAutoCalTime();
    long getAutoCalInterval();

    int getExportDataProcess();
    boolean getExportDataResult();
    void exportData(long start,long end);
}
