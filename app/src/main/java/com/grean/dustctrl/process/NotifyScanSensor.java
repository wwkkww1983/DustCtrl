package com.grean.dustctrl.process;

/**
 * 显示传感器结果
 * Created by Administrator on 2017/8/28.
 */

public interface NotifyScanSensor {
    void onResult(SensorData data);
    void setAlarmDust(boolean alarm);
}
