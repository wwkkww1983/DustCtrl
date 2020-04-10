package com.grean.dustctrl.device;

import android.util.Log;

import com.grean.dustctrl.ComReceiveProtocol;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by weifeng on 2020/4/10.
 */

public class WeatherWs5pUltrasonic implements WeatherControl,ComReceiveProtocol {
    final static String tag = "WeatherWs5pUltrasonic";
    private final static byte[] cmdInquire = {0x01,0x03,0x00,0x01,0x00,0x0a, (byte) 0x94, (byte) 0x0d};
    private SensorData data;
    private SerialCommunicationController com;

    public WeatherWs5pUltrasonic(SerialCommunicationController com,SensorData data){
        this.data = data;
        this.com = com;
        this.com.setComReceiveProtocol(this);
    }

    boolean checkModBus(byte[] rec,int size){
        if(size != 25){
            return false;
        }
        if(rec[0]!=0x01){
            return false;
        }
        if(rec[1]!=0x03){
            return false;
        }
        if(tools.calcCrc16(rec,0,size)!=0){
            return false;
        }
        return true;
    }

    @Override
    public void inquire() {
        com.send(cmdInquire,WeatherInquire);
    }

    @Override
    public void receiveProtocol(byte[] rec, int size, int state) {
        if(checkModBus(rec,size)){
            switch (state){
                case WeatherInquire:
                    //Log.d(tag,tools.bytesToHexString(rec,size));
                    float f = tools.getFloatReversedOder(rec,6);
                    data.setWindForce(f);
                    //if(data.getWindForce() != 0f){//仅风速有变化时改变风向，风速为0时风向不变
                    f = tools.getFloatReversedOder(rec,10);
                    data.setWindDirection(f);
                    f = tools.getFloatReversedOder(rec,14);//温度
                    data.setAirTemperature(f);
                    f = tools.getFloatReversedOder(rec,18);//湿度
                    data.setAirHumidity(f);
                    f = tools.getFloatReversedOder(rec,22);//大气压
                    data.setAirPressure(f);

                    break;
                default:

                    break;
            }
        }
    }

    @Override
    public void receiveAsyncProtocol(byte[] rec, int size) {

    }
}
