package com.grean.dustctrl.device;

import com.grean.dustctrl.NoiseCalibrationListener;

/**
 * Created by weifeng on 2020/3/2.
 */

public interface NoiseControl {
    int NoiseGetRealTimeData = 201,
            NoiseCalibration = 202;

    /**
     * 请求瞬时数据
     */
    void inquire();

    /**
     * 在线校准声级计方法
     * @param listener
     */
    void calibrationNoise(NoiseCalibrationListener listener);
}
