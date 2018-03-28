package com.grean.dustctrl.protocol;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/11/6.
 */

public interface ClientDataBaseCtrl {
    /**
     * 存储最近一条分钟数据
     * @param now 数据时间
     */
    void saveMinData(long now);
    void getRealTimeData(float [] realTimeData);
}
