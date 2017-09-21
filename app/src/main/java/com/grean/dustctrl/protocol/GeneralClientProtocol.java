package com.grean.dustctrl.protocol;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/9/5.
 */

public interface GeneralClientProtocol {
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
}
