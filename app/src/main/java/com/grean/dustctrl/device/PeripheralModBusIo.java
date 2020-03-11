package com.grean.dustctrl.device;


import com.grean.dustctrl.ComReceiveProtocol;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * 设计为4入4出，输出口 0为风扇；1暂无；2实时超标报警；3分钟数据超标报警
 * Created by weifeng on 2020/3/4.
 */

public class PeripheralModBusIo implements PeripheralControl,ComReceiveProtocol {
    private SensorData data;
    private SerialCommunicationController com;

    public PeripheralModBusIo(SerialCommunicationController com, SensorData data){
        this.com = com;
        this.data = data;
        this.com.setComReceiveProtocol(this);
    }

    private void setRelay(int num,boolean key){
        byte [] currentFrame = new byte[8];
        currentFrame[0] = 0x01;
        currentFrame[1] = 0x05;
        currentFrame[2] = 0x00;
        if((num < 4)&&(num >= 0)) {
            currentFrame[3] = (byte) num;
            if(key) {
                currentFrame[4] = (byte) 0xff;
            }else{
                currentFrame[4] = 0x00;
            }
            currentFrame[5] = 0x00;
            tools.addCrc16(currentFrame, 0, 6);
            com.send(currentFrame,PeripheralSetRelay);
        }
    }

    @Override
    public void fanRelay() {
        if(data.getInnerTemp() > 30f){
            setRelay(0,true);
            data.setCtrlDo(0,true);
        }else if(data.getInnerTemp() < 20f){
            setRelay(0,false);
            data.setCtrlDo(0,false);
        }
    }

    @Override
    public void realTimeDataRelay(boolean key) {
        setRelay(2,key);
        data.setCtrlDo(2,key);
    }

    @Override
    public void minDataRelay(boolean key) {
        setRelay(3,key);
        data.setCtrlDo(3,key);
    }

    @Override
    public void controlRelays(int num, boolean key) {
        setRelay(num,key);
    }

    @Override
    public void receiveProtocol(byte[] rec, int size, int state) {
        switch (state){
            case PeripheralSetRelay:

                break;
            default:
                break;
        }
    }

    @Override
    public void receiveAsyncProtocol(byte[] rec, int size) {

    }
}
