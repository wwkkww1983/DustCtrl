package com.grean.dustctrl.protocol;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/9/5.
 */

public interface GeneralClientProtocol {
    public static final int AlARM_N =1,//正常
    ALARM_C=2,//校准
            ALARM_D=3,//设备断开
            ALARM_P=4,//电源异常
            ALARM_L=5,//超量程下限
            ALARM_H=6,//超量程上限
            ALARM_SUB=7,//超设定下限
            ALARM_ADD=8,//超设定上限
            ALARM_GREAT_THAN=9,//颗粒物有效数据大于90%，噪声总采集率大于95%
            ALARM_LESS_THAN=10,//颗粒物有效数据小于90%，噪声总采集率小于95%
            ALARM_S=11,//风速大于5m/s
            ALARM_R=12,//雨、雪、雷、电
            ALARM_A=13;//补传
    /**
     * 处理接收数据
     * @param rec
     * @param count
     */
    void handleProtocol(byte[] rec,int count);

    /**
     * 设置地区码
     * @param string
     */
    void setMnCode(String string);

    /**
     * 插入需要发送的队列
     * @param string
     * @return
     */
    boolean addSendBuff(String string);

    /**
     * 启动心跳包
     */
    void startHeartBeatPacket();

    /**
     * 停止心跳包
     */
    void stopHeartBeatPacket();

    /**
     * 写入实时数据
     * @param data
     */
    void setRealTimeData(SensorData data);

    void setRealTimeAlarm(int alarm);
}
