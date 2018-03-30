package com.grean.dustctrl.protocol;

import android.os.Handler;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2017/11/6.
 */

public interface ClientDataBaseCtrl {
    public static final int UPDATE_REAL_TIME =1;
    /**
     * 存储最近一条分钟数据
     * @param now 数据时间
     */
    void saveMinData(long now);
    void getRealTimeData(Handler handle);
}
