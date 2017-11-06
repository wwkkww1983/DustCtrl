package com.grean.dustctrl.protocol;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/11/6.
 */

public interface ClientDataBaseCtrl {
    void saveMinData(long now);
    void getRealTimeData(float [] realTimeData);
}
