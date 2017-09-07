package com.grean.dustctrl.protocol;

import com.grean.dustctrl.process.SensorData;

/**
 * 一般系统系统数据、状态、设置查询
 * Created by weifeng on 2017/9/7.
 */

public interface GeneralInfoProtocol {
    String getSystemState();
    SensorData getSensorData();
    void notifySenorData(SensorData data);


}
