package com.grean.dustctrl;

import android.util.Log;

import com.SerialCommunication;

/**
 * 噪声仪通讯
 * Created by Administrator on 2017/8/24.
 */

public class NoiseCommunication extends SerialCommunication{
    private static final String tag = "NoiseCommunication";
    private float noiseData;
    public static final int NoiseRealTimeData = 1,
            Other = 0;
    private static final byte[] cmdNoiseRealTimeData={'A','W','A','0'};

    private static NoiseCommunication instance = new NoiseCommunication();

    private NoiseCommunication(){
        super(1,9600,0);
    }

    public static NoiseCommunication getInstance() {
        return instance;
    }

    @Override
    protected boolean checkRecBuff() {
        return true;
    }

    @Override
    protected void communicationProtocol(byte[] rec, int size,int state) {
        if(checkSum(rec,size)){
            String recString = new String(rec,5,5);
            noiseData = Float.valueOf(recString);
            //Log.d(tag,"data = "+String.valueOf(noiseData));
        }
    }

    public float getNoiseData() {
        return noiseData;
    }

    /**
     * 检查校验和
     * @param rec
     * @param count
     * @return
     */
    private boolean checkSum(byte [] rec,int count){
        String string = new String(rec,0,4);

        if(!string.equals("AWAA")){
            return false;
        }

        int sum=0;
        if(count >3) {
            for (int i = 0; i < (count - 2); i++) {
                sum+=rec[i];
            }
            sum = sum&0x0000ffff;
            int end = rec[count-1];
            end = end<<8;
            end += rec[count-2];
            //Log.d(tag,"count = "+String.valueOf(count)+"sum="+String.valueOf(sum)+"end=" + String.valueOf(end));
            if(end!=sum){
                return false;
            }else {
                return true;
            }
        }else{
            return false;
        }
    }

    @Override
    protected void asyncCommunicationProtocol(byte[] rec, int size) {

    }

    public void sendFrame(int cmd){
        switch (cmd){
            case NoiseRealTimeData:
                addSendBuff(cmdNoiseRealTimeData,cmd);
                break;
            case Other:

                break;
            default:

                break;
        }
    }

}
