package com.grean.dustctrl.dust;

import com.grean.dustctrl.process.DustMeterInfo;
import com.grean.dustctrl.process.SensorData;

/**
 * Created by weifeng on 2018/4/2.
 */

public interface DustMeterController {
    int Dust = 2,
            DustMeterStop =3,
            DustMeterRun=4,
            DustMeterPumpTime=5,
            DustMeterLaserTime=6,
            DustMeterBgStart=7,
            DustMeterBgEnd=8,
            DustMeterBgResult=9,
            DustMeterSpanStart=10,
            DustMeterSpanEnd=11,
            DustMeterSpanResult=12;
    byte[] getReadCpmCmd();
    byte[] getStopCmd();
    byte[] getRunCmd();
    byte[] getPumpTimeCmd();
    byte[] getLaserTimeCmd();
    byte[] getBgStartCmd();
    byte[] getBgEndCmd();
    byte[] getBgResultCmd();
    byte[] getSpanStartCmd();
    byte[] getSpanEndCmd();
    byte[] getSpanResult();
    byte[] getValveOnCmd();
    byte[] getValveOffCmd();
    byte[] getMotorStop();
    byte[] getMotorForward();
    byte[] getMotorBackward();
    void handleProtocol(byte[] rec, int size, int state, SensorData data, DustMeterInfo info);
}
