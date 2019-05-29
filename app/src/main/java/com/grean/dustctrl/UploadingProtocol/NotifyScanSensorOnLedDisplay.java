package com.grean.dustctrl.UploadingProtocol;


import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2019/5/29.
 */

public interface NotifyScanSensorOnLedDisplay {
    void onResult(SensorData data);
}
