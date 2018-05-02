package com.grean.dustctrl.hardware;

import com.grean.dustctrl.CtrlCommunication;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by weifeng on 2018/5/2.
 */

public class MainBoardV1_0 implements MainBoardController{
    private static final byte[] cmdInquire = {0x55,0x03,0x20,0x01,0x00,0x1f,0x53, (byte) 0xd6};
    private static final byte[] cmdAirData={(byte) 0xe3,0x03,0x00,0x00,0x00,0x03,0x12,0x49};
    private static final byte[] cmdWindForce = {(byte) 0xe1,0x03,0x00,0x00,0x00,0x01, (byte) 0x92,0x6a};
    private static final byte[] cmdWindDirection = {(byte) 0xe2,0x03,0x00,0x00,0x00,0x01, (byte) 0x92,0x59};

    @Override
    public void inquireState(byte[] rec, int size, SensorData data) {
        if (rec[2]==0x3e) {
            for(int i=0;i<5;i++) {
                if (rec[4+i*2] == 0x00) {
                    data.setCtrlDo(i, false);
                } else {
                    data.setCtrlDo(i, true);
                }
            }
            if(rec[38]==0x00){
                data.setAcIn(false);
            }else{
                data.setAcIn(true);
            }

            if(rec[40]==0x00){
                data.setBatteryLow(true);
            }else{
                data.setBatteryLow(false);
            }
            data.setHiTemp(tools.getFloat(rec,44));
            data.setLoTemp(tools.getFloat(rec,48));
            data.setHiHumidity(tools.getFloat(rec,52));
            data.setLoHumidity(tools.getFloat(rec,56));
            data.setHeatPwm(tools.byte2int(rec,57));
            data.setMotorState(tools.byte2int(rec,59));
            data.setMotorRounds(tools.byte2int(rec,61));
            data.setMotorTime(tools.byte2int(rec,63));
        }
    }

    @Override
    public void inquireWindForce(byte[] rec, int size, SensorData data) {
        int intDate = tools.byte2int(rec,3);
        float floatData = ((float)intDate)/10.0f;
        data.setWindForce(floatData);
    }

    @Override
    public void inquireWindDir(byte[] rec, int size, SensorData data) {
        int intDate = tools.byte2int(rec,3);
        float floatData = (float)intDate;
        data.setWindDirection(floatData);
    }

    @Override
    public void inquireAirParameter(byte[] rec, int size, SensorData data) {
        int intDate = tools.byte2int(rec,3);
        float floatData = ((float)intDate)/10.0f;
        data.setAirPressure(floatData);
        intDate = tools.byte2int(rec,5);
        // intDate = 65532;
        //处理负数
        if(intDate >= 32768){
            intDate -= 65536;
        }
        floatData = ((float)intDate)/10.0f;
        data.setAirTemperature(floatData);
        intDate = tools.byte2int(rec,7);
        //Log.d(tag,"Humidity"+String.valueOf(intDate));
        floatData = ((float)intDate)/10.0f;
        data.setAirHumidity(floatData);
    }

    @Override
    public void sendInquireCmd(int cmd, SerialCommunicationController controller) {
        switch (cmd){
            case CtrlCommunication.Inquire:
                controller.send(cmdInquire,cmd);
                break;
            case CtrlCommunication.AirParameter:
                controller.send(cmdAirData,cmd);
                break;
            case CtrlCommunication.WindDirection:
                controller.send(cmdWindDirection,cmd);
                break;
            case CtrlCommunication.WindForce:
                controller.send(cmdWindForce,cmd);
                break;
            default:

                break;

        }
    }
}
