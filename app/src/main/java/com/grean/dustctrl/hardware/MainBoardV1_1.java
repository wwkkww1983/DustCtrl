package com.grean.dustctrl.hardware;

import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by weifeng on 2018/5/2.
 */

public class MainBoardV1_1 implements MainBoardController {
    private static final byte[] cmdInquire = {0x55,0x03,0x20,0x01,0x00,0x1f,0x53, (byte) 0xd6};
    @Override
    public void inquireState(byte[] rec, int size, SensorData data) {
        if (rec[2]==0x3e) {
            for(int i=0;i<5;i++) {//开关量输出
                if (rec[4+i*2] == 0x00) {
                    data.setCtrlDo(i, false);
                } else {
                    data.setCtrlDo(i, true);
                }
            }
            data.setAirTemperature(tools.getFloat(rec,16));//大气温度
            data.setAirHumidity(tools.getFloat(rec,20));//大气湿度
            //21~24 备用
            data.setAirPressure(tools.getFloat(rec,28));//大气压
            data.setWindForce(tools.getFloat(rec,32));//风速
            data.setWindDirection(tools.getFloat(rec,36));//风向
            if(rec[37]==0x00){//自动校准滑块，限位开关。限位开关按压时为true
                data.setCalPos(false);
            }else{
                data.setCalPos(true);
            }

            if(rec[38]==0x00){//外接电源输入，当外接电源时为true
                data.setAcIn(false);
            }else{
                data.setAcIn(true);
            }

            if(rec[39]==0x00){//自动校准滑块，测量位置检测开关，当未处于检测位置时为true
                data.setMeasurePos(false);
            }else{
                data.setMeasurePos(true);
            }

            if(rec[40]==0x00){//电池电压，电池电压高为true
                data.setBatteryLow(true);
            }else{
                data.setBatteryLow(false);
            }
            data.setHiTemp(tools.getFloat(rec,44));//采样管温度
            data.setLoTemp(tools.getFloat(rec,48));//采样管出口温度
            data.setHiHumidity(tools.getFloat(rec,52));//采样管目标温度
            data.setPipeTemp(tools.getFloat(rec,52));//同上
            data.setLoHumidity(tools.getFloat(rec,56));//采样管出口湿度
            data.setHeatPwm(tools.byte2int(rec,57));//加热pwm系数2~1000 数字越高加热功率越大
            data.setMotorState(tools.byte2int(rec,59));//电机状态：停止；正转；反转
            data.setMotorRounds(tools.byte2int(rec,61));//当前步进步数 200step/round
            data.setMotorTime(tools.byte2int(rec,63));//步进电机步进时间
        }
    }

    @Override
    public void inquireWindForce(byte[] rec, int size, SensorData data) {

    }

    @Override
    public void inquireWindDir(byte[] rec, int size, SensorData data) {

    }

    @Override
    public void inquireAirParameter(byte[] rec, int size, SensorData data) {

    }

    @Override
    public void sendInquireCmd(int cmd ,SerialCommunicationController controller) {
        switch (cmd){
            case CtrlCommunication.Inquire:
                controller.send(cmdInquire,cmd);
                break;
            default:

                break;

        }
    }
}
