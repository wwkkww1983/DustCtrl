package com.grean.dustctrl.dust;

import android.util.Log;

import com.grean.dustctrl.process.DustMeterInfo;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by weifeng on 2019/12/9.
 */

public class LyjdLpm1000 implements DustMeterController{
    private static final String tag = "LyjdLpm1000";
    private static final byte[] cmdDustMeterCpm = {(byte) 0x01,0x03,0x00,0x00,0x00,0x02, (byte) 0xc4, (byte) 0x0b};
    private static final byte[] cmdDustMeter={(byte) 0xdd,0x03,0x00,0x07,0x00,0x01,0x26, (byte) 0x97};

    @Override
    public byte[] getReadCpmCmd() {
        return cmdDustMeterCpm;
    }

    @Override
    public byte[] getStopCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getRunCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getPumpTimeCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getLaserTimeCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getBgStartCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getBgEndCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getBgResultCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getSpanStartCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getSpanEndCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getSpanResult() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getValveOnCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getValveOffCmd() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getMotorStop() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getMotorForward() {
        return cmdDustMeter;
    }

    @Override
    public byte[] getMotorBackward() {
        return cmdDustMeter;
    }

    @Override
    public void handleProtocol(byte[] rec, int size, int state, SensorData data, DustMeterInfo info) {
        switch(state){
            case Dust:
                float intDust = tools.getFloat(rec,6);
                data.setValue(intDust);
                Log.d(tag,"value="+String.valueOf(data.getValue()));
                break;
            default:

                break;
        }

    }
}
