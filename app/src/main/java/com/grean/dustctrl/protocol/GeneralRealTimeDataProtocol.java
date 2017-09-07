package com.grean.dustctrl.protocol;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/9/7.
 */

public interface GeneralRealTimeDataProtocol {
    SensorData getRealTimeData();
}
