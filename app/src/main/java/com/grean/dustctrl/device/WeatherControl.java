package com.grean.dustctrl.device;

/**
 * Created by weifeng on 2020/3/2.
 */

public interface WeatherControl {
    int WeatherInquire = 401;
    /**
     * 请求瞬时数据
     */
    void inquire();
}
