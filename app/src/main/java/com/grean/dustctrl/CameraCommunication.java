package com.grean.dustctrl;

import android.util.Log;

import com.SerialCommunication;
import com.tools;

/**
 * Created by weifeng on 2018/12/12.
 * 使用海康 DS-2DF8225IH-A 主控软件版本 5.5.12build181211 设置IP 192.168.1.64 其他默认
 * RS-584设置 9600 8 无 1 解码器类型PELCO-D 地址 0
 * RS485默认地址 0x01 安装时请保持传感器零方位角和摄像机零方位脚一致摄像机方位角可通过控件进行设置
 */

public class CameraCommunication extends SerialCommunication{
    private static final String tag = "CameraCommunication";
    private static CameraCommunication instance = new CameraCommunication();
    private int windDirection,directionOffset;

    public static CameraCommunication getInstance (){
        return instance;
    }

    private CameraCommunication (){
        super(2,9600,0);
    }

    @Override
    protected boolean checkRecBuff() {
        return true;
    }

    public void setDirectionOffset(int directionOffset) {
        this.directionOffset = directionOffset;
    }

    @Override
    protected void communicationProtocol(byte[] rec, int size, int state) {
        //Log.d(tag,tools.bytesToHexString(rec,size));
    }

    @Override
    protected void asyncCommunicationProtocol(byte[] rec, int size) {
        //Log.d(tag,"Async rec = "+tools.bytesToHexString(rec,size));
        if(checkFrame(rec,size)){
            windDirection = (int) CtrlCommunication.getInstance().getData().getWindDirection()+directionOffset;
            if(windDirection >=360){
                windDirection -= 360;
            }else if(windDirection <0){
                windDirection += 360;
            }else{

            }
            byte [] cmd = new byte[7];
            cmd[0] = 0x01;
            cmd[1] = 0x03;
            cmd[2] = 0x02;
            byte [] tempBuff = tools.int2byte(windDirection);
            cmd[3] = tempBuff[0];
            cmd[4] = tempBuff[1];
            tools.addCrc16(cmd,0,5);
            //Log.d(tag,"send = "+tools.bytesToHexString(cmd,cmd.length));
            addSendBuff(cmd,0);
        }
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
