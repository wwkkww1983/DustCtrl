package com.grean.dustctrl.device;

import com.grean.dustctrl.ComReceiveProtocol;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * 厂家为新普惠mini超声波气象五参
 * Created by weifeng on 2020/3/4.
 */

public class WeatherMiniUltrasonic implements WeatherControl,ComReceiveProtocol{
    private final static byte[] cmdInquire = {0x01,0x03,0x00,0x00,0x00,0x05, (byte) 0x85, (byte) 0xc9};
    private SensorData data;
    private SerialCommunicationController com;

    public WeatherMiniUltrasonic(SerialCommunicationController com,SensorData data){
        this.com = com;
        this.data = data;
        this.com.setComReceiveProtocol(this);
    }

    @Override
    public void inquire() {
        com.send(cmdInquire,WeatherInquire);
    }

    boolean checkModBus(byte[] rec,int size){
        if(size != 15){
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
    public void receiveProtocol(byte[] rec, int size, int state) {
        if(checkModBus(rec,size)){
            switch (state){
                case WeatherInquire:
                    int i = tools.byte2int(rec,3);
                    float f;
                    if(i != 0x07ff){//有效值
                        f = (float) i/100f;
                        data.setWindForce(f);
                    }
                    i = tools.byte2int(rec,5);//
                    if(i != 0x07ff){//有效值
                        f = (float) i;
                        data.setWindDirection(f);
                    }
                    i = tools.byte2int(rec,7);//温度
                    if(i != 0x07ff){//有效值
                        if(i>=0x1000){//负数
                            f = (float) (i-0x10000)/10f;
                        }else{
                            f = (float) i/10f;
                        }
                        data.setAirTemperature(f);
                    }
                    i = tools.byte2int(rec,9);//湿度
                    if(i != 0x07ff){//有效值
                        f = (float) i/10f;
                        data.setAirHumidity(f);
                    }
                    i = tools.byte2int(rec,11);//大气压 hPa
                    if(i != 0x07ff){//有效值
                        f = (float) i/10;
                        data.setAirPressure(f);
                    }
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
