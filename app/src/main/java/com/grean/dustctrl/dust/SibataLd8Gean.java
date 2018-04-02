package com.grean.dustctrl.dust;

import android.util.Log;

import com.grean.dustctrl.process.DustMeterInfo;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by weifeng on 2018/4/2.
 */

public class SibataLd8Gean implements DustMeterController{
    private static final String tag = "SibataLd8Gean";
    private static final byte[] cmdDustMeterCpm = {(byte) 0xdd,0x03,0x00,0x01,0x00,0x01, (byte) 0xc6, (byte) 0x96};
    private static final byte[] cmdStopDustMeter = {(byte) 0xdd,0x06,0x00,0x03,0x00,0x00,0x6a, (byte) 0x96};
    private static final byte[] cmdRunDustMeter = {(byte) 0xdd,0x06,0x00,0x03,0x00,0x01, (byte) 0xab,0x56};
    private static final byte[] cmdDustMeterPumpTime = {(byte) 0xdd,0x03,0x00,0x04,0x00,0x01, (byte) 0xd6, (byte) 0x97};
    private static final byte[] cmdDustMeterLaserTime = {(byte) 0xdd,0x03,0x00,0x05,0x00,0x01, (byte) 0x87,0x57};
    private static final byte[] cmdDustMeterBgStart = {(byte) 0xdd,0x06,0x00,0x06,0x00,0x01, (byte) 0xbb,0x57};
    private static final byte[] cmdDustMeterBgEnd={(byte) 0xdd,0x06,0x00,0x06,0x00,0x00,0x7a, (byte) 0x97};
    private static final byte[] cmdDustMeterBgResult={(byte) 0xdd,0x03,0x00,0x07,0x00,0x01,0x26, (byte) 0x97};
    private static final byte[] cmdDustMeterSpanStart={(byte) 0xdd,0x06,0x00,0x08,0x00,0x01, (byte) 0xda, (byte) 0x94};
    private static final byte[] cmdDustMeterSpanEnd={(byte) 0xdd,0x06,0x00,0x08,0x00,0x00,0x1b,0x54};
    private static final byte[] cmdDustMeterSpanResult={(byte) 0xdd,0x03,0x00,0x09,0x00,0x01,0x47,0x54};
    @Override
    public byte[] getReadCpmCmd() {
        return cmdDustMeterCpm;
    }

    @Override
    public byte[] getStopCmd() {
        return cmdStopDustMeter;
    }

    @Override
    public byte[] getRunCmd() {
        return cmdRunDustMeter;
    }

    @Override
    public byte[] getPumpTimeCmd() {
        return cmdDustMeterPumpTime;
    }

    @Override
    public byte[] getLaserTimeCmd() {
        return cmdDustMeterLaserTime;
    }

    @Override
    public byte[] getBgStartCmd() {
        return cmdDustMeterBgStart;
    }

    @Override
    public byte[] getBgEndCmd() {
        return cmdDustMeterBgEnd;
    }

    @Override
    public byte[] getBgResultCmd() {
        return cmdDustMeterBgResult;
    }

    @Override
    public byte[] getSpanStartCmd() {
        return cmdDustMeterSpanStart;
    }

    @Override
    public byte[] getSpanEndCmd() {
        return cmdDustMeterSpanEnd;
    }

    @Override
    public byte[] getSpanResult() {
        return cmdDustMeterSpanResult;
    }

    @Override
    public void handleProtocol(byte[] rec, int size, int state, SensorData data, DustMeterInfo info) {
        switch(state){
            case Dust:
                int intDust = tools.byte2int(rec,3);
                data.setValue(intDust);
                // Log.d(tag,"value="+String.valueOf(data.getValue()));
                break;
            case DustMeterPumpTime:
                int intTime = tools.byte2int(rec,3);
                info.setPumpTime(intTime);
                Log.d(tag,"DustMeterPumpTime"+String.valueOf(intTime));
                break;
            case DustMeterLaserTime:
                int intLaser = tools.byte2int(rec,3);
                info.setLaserTime(intLaser);
                Log.d(tag,"DustMeterLaserTime"+String.valueOf(intLaser));
                break;
            case DustMeterBgResult:
                if (rec[4]==0x01){
                    info.setBgOk(true);
                }else{
                    info.setBgOk(false);
                }
                Log.d(tag,"BgResult:"+String.valueOf(info.isBgOk()));
                break;
            case DustMeterSpanResult:
                if (rec[4]==0x01){
                    info.setSpanOk(true);
                }else{
                    info.setSpanOk(false);
                }
                Log.d(tag,"SpanResult:"+String.valueOf(info.isSpanOk()));
                break;
            default:
                break;
        }
    }
}
