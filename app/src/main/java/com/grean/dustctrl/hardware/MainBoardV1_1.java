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
            for(int i=0;i<5;i++) {
                if (rec[4+i*2] == 0x00) {
                    data.setCtrlDo(i, false);
                } else {
                    data.setCtrlDo(i, true);
                }
            }
            data.setAirTemperature(tools.getFloat(rec,16));
            data.setAirHumidity(tools.getFloat(rec,20));
            data.setAirPressure(tools.getFloat(rec,28));
            data.setWindForce(tools.getFloat(rec,32));
            data.setWindDirection(tools.getFloat(rec,36));
            if(rec[37]==0x00){
                data.setCalPos(false);
            }else{
                data.setCalPos(true);
            }

            if(rec[38]==0x00){
                data.setAcIn(false);
            }else{
                data.setAcIn(true);
            }

            if(rec[39]==0x00){
                data.setMeasurePos(false);
            }else{
                data.setMeasurePos(true);
            }

            if(rec[40]==0x00){
                data.setBatteryLow(true);
            }else{
                data.setBatteryLow(false);
            }
            data.setHiTemp(tools.getFloat(rec,44));
            data.setLoTemp(tools.getFloat(rec,48));
            data.setHiHumidity(tools.getFloat(rec,52));
            data.setPipeTemp(tools.getFloat(rec,52));
            data.setLoHumidity(tools.getFloat(rec,56));
            data.setHeatPwm(tools.byte2int(rec,57));
            data.setMotorState(tools.byte2int(rec,59));
            data.setMotorRounds(tools.byte2int(rec,61));
            data.setMotorTime(tools.byte2int(rec,63));
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
