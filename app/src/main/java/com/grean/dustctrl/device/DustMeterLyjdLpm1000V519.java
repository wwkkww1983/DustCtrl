package com.grean.dustctrl.device;

import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * 对应朗亿机电软件版本519，修改读取实时值为寄存器4001 int型数据
 * Created by weifeng on 2020/4/14.
 */

public class DustMeterLyjdLpm1000V519 extends DustMeterLyjdLpm1000 {
    public DustMeterLyjdLpm1000V519(SerialCommunicationController com, SensorData data) {
        super(com, data);
    }

    @Override
    public void receiveProtocol(byte[] rec, int size, int state) {
        if (tools.checkFrameWithAddr(rec,size,(byte)0x01)) {
            float f;
            switch (state) {
                case Inquire:
                    f = tools.getFloat(rec,6);//环境湿度
                    data.setLoHumidity(f);
                    f = tools.getFloat(rec,10);//环境温度
                    data.setLoTemp(f);
                    f = tools.getFloat(rec,14);//加热管温度
                    data.setPipeTemp(f);
                    data.setHiHumidity(f);//主界面显示 加热管温度
                    f = tools.getFloat(rec,22);//加热管PWM
                    data.setHeatPwm((int) f);
                    //Log.d(tag,"inquire = "+String.valueOf(data.getLoHumidity())+";"+String.valueOf(data.getLoTemp()+";"+
                    //String.valueOf(data.getPipeTemp())+";"+String.valueOf(data.getHeatPwm())));
                    break;
                case DustCpm:
                    int intDust = tools.byte2int(rec,5);
                    data.setValue(intDust);
                    f = tools.getFloat(rec,14);//流量
                    data.setFlow(f);
                    f = tools.getFloat(rec,22);//仪器内部温度
                    data.setInnerTemp(f);
                    //Log.d(tag,"DustCpm="+tools.bytesToHexString(rec,size));
                    break;
                case DustMeterCalibrationState:
                    //Log.d(tag,"DustMeterCalibrationState="+tools.bytesToHexString(rec,size));
                    if(rec[4] == 0x00){//测量状态
                        measuring  =true;
                    }else{
                        measuring = false;
                    }
                    break;
                default:

                    break;
            }
        }
    }
}
