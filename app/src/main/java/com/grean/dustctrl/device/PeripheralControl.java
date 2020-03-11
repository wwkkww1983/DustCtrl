package com.grean.dustctrl.device;

/**
 * Created by weifeng on 2020/3/2.
 */

public interface PeripheralControl {
    int PeripheralSetRelay = 301;
    void fanRelay();
    void realTimeDataRelay(boolean key);
    void minDataRelay(boolean key);
    void controlRelays(int num,boolean key);
}
