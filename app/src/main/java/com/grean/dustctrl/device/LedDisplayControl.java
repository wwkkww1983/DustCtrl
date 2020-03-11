package com.grean.dustctrl.device;

import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2020/3/2.
 */

public interface LedDisplayControl {
    void onResult(SensorData data);
    void startServer();
}
