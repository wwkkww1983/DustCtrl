package com.grean.dustctrl.device;

import android.util.Log;

import com.grean.dustctrl.ComReceiveProtocol;
import com.grean.dustctrl.SerialCommunicationController;
import com.grean.dustctrl.UploadingProtocol.*;
import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by weifeng on 2020/3/3.
 */

public class CameraBlack implements ComReceiveProtocol, CameraControl {
    private static final String tag = "CameraBlack";
    private SerialCommunicationController com;
    private SensorData data;
    private int windDirection,directionOffset;
    public CameraBlack(SerialCommunicationController com, SensorData data){
        this.com = com;
        this.data= data;
        this.com.setComReceiveProtocol(this);
    }

    @Override
    public void receiveProtocol(byte[] rec, int size, int state) {

    }

    @Override
    public void receiveAsyncProtocol(byte[] rec, int size) {
        if(checkFrame(rec,size)){
            windDirection = (int) data.getWindDirection();
            int direction = windDirection+directionOffset;
            if(direction >=360){
                direction -= 360;
            }else if(direction <0){
                direction += 360;
            }else{

            }
            byte [] cmd = new byte[7];
            cmd[0] = 0x01;
            cmd[1] = 0x03;
            cmd[2] = 0x02;
            byte [] tempBuff = tools.int2byte(direction);
            cmd[3] = tempBuff[0];
            cmd[4] = tempBuff[1];
            tools.addCrc16(cmd,0,5);
            //Log.d(tag,"send = "+tools.bytesToHexString(cmd,cmd.length));
            com.send(cmd,CameraSetDirection);
        }
        //Log.d(tag,tools.bytesToHexString(rec,size));
    }

    @Override
    public void setDirectionOffset(int directionOffset) {
        this.directionOffset = directionOffset;
    }

    @Override
    public int getDirectionOffset() {
        return directionOffset;
    }


    @Override
    public void startServer() {
        Log.d(tag,"start camera server");

    }


    //查询命令为 0103000100021814
    private boolean checkFrame(byte[] rec,int size){
        final byte[] rightFrame = {0x01,0x03,0x00,0x01,0x00,0x02,0x18,0x14};
        if(size!=8){
            return false;
        }

        for(int i=0;i<8;i++){
            if(rec[i]!=rightFrame[i]){
                return false;
            }
        }
        return true;
    }
}
