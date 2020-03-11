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

    void setDustParaK(float paraK);
    void setDustParaB(float paraB);

    void calDustMeter();

    void setDustMeterResult(boolean bg,boolean span);

    boolean getDustMeterBg();
    boolean getDustMeterSpan();
    void setDustMeterPumpTime(int pumpTime);
    void setDustMeterLaserTime(int laserTime);
    int getDustMeterPumpTime();
    int getDustMeterLaserTime();

    void setDustCalMeterProcess(int process);
    int getDustMeterCalProcess();

    GeneralHistoryDataFormat getHistoryData(long endDate);
    GeneralHistoryDataFormat getHistoryData(long startDate,long endDate);
    GeneralHistoryDataFormat getHistoryHourData(long startDate,long endDate);

    ArrayList<String> getLog(long dateStart);
    ArrayList<String> getLog(long startDate,long endDate);

    float getParaK();
    float getParaB();
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

    /**
     * 获取协议名称
     * @return
     */
    String[] getClientProtocolNames();

    /**
     * 获取当前协议编号 0~
     * @return
     */
    int getClientProtocolName();

    /**
     * 获取当前扬尘参数名称字符串数组
     * @return
     */
    String[] getDustNames();

    /**
     * 获取当前扬尘参数名字
     * @return
     */
    int getDustName();
    void setDustName(int name);
    float getAlarmDust();
    void setAlarmDust(float alarm);
    void setClientProtocol(int name);
    void calDustMeterZero();
    void setAlarmMark(boolean alarm);
    boolean getAlarmMark();
    boolean isServerConnected();
    void setSystemDate(int year,int mon,int day,int hour,int min,int second);

    int getMotorTime();
    int getMotorStep();
    void setMotorTime(int time);
    void setMotorStep(int time);

    void setRelay(int num , boolean key);
    void ForwardTest();
    void BackwardTest();
    void ForwardStep();
    void BackwardStep();

    boolean isDustMeterRun();
    void setDustMeterRun(boolean key);

    /**
     * 开始校准声级计
     */
    void calibrationNoise();

    /**
     * 获取声级计校准状态
     * @return =0 正在校准 =1 校准成功 =2校准失败
     */
    int getCalibrationNoiseState();

    /**
     * 获取粉尘仪型号名称集合
     * @return
     */
    String[] getDustMeterNames();

    /**
     * 获取当前粉尘仪名称
     * @return
     */
    int getDustMeter();

    void setDustMeter(int name);

    int getCameraOffset();
    void setCameraOffset(int offset);

    boolean isCameraEnable();
    void setCameraEnable(boolean enable);

    /**
     * 固化上传配置
     * @param configString
     */
    void saveUploadingConfig(String configString);
}
