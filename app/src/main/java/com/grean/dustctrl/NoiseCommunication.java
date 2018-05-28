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
    private boolean calOk;
    private NoiseCalibrationListener calibrationListener;
    public static final int NoiseRealTimeData = 1,
            Other = 0;
    private static final byte[] cmdNoiseRealTimeData={'A','W','A','0'};
    private static final byte[] cmdAutoCal={'A','W','A','O','1'};
    private String calInfo = "Error";

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
        //Log.d(tag,new String(rec,0,size));
        if(checkSum(rec,size)){
            String[] content = new String(rec,0,size).split(",");
            String cmd = content[0];
            if(cmd.equals("AWAA")) {
                String recString = content[1].substring(0,content[1].indexOf("d"));
                noiseData = Float.valueOf(recString);
                //Log.d(tag,"cmd = "+new String(rec, 0, size)+"data = "+String.valueOf(noiseData));
            }else if(cmd.equals("AWAV")){
                /*calInfo = new String(rec,0,size);
                if(calibrationListener!=null) {
                    calibrationListener.onResult(calInfo);
                }
                Log.d(tag,calInfo);*/
            }else{

            }

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

        if(!((string.equals("AWAA"))||(string.equals("AWAV")))){
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
       // Log.d(tag,new String(rec,0,size));
        String cmd = new String(rec,0,4);
        String cmdRsTech = new String(rec,0,2);
        if(cmd.equals("AWAV")){
            calInfo = new String(rec,0,size);
            String[]strings = calInfo.split(",");
            if((strings.length>4)&&(strings[1].equals("E"))) {
                //Log.d(tag, strings[3]);
                float value = Float.valueOf(strings[3].substring(0,strings[3].indexOf("dB")));
                //Log.d(tag, String.valueOf(value));
                if((value >=88)&&(value <=92)){
                    calOk = true;
                }else{
                    calOk = false;
                }
                if(calibrationListener!=null) {
                    calibrationListener.onResult(calInfo,calOk);
                }
            }
           // Log.d(tag,calInfo);
        }else if(cmdRsTech.equals("aa")&&(size==6)) {//支持北京瑞森新谱声级计
            try {
                noiseData = Float.valueOf(new String(rec,2,4))/10f;
            }catch (Exception e){

            }
        }else{
            Log.d(tag,new String(rec,0,size));
        }
    }

    public void sendCalibrationCmd(NoiseCalibrationListener listener){
        this.calibrationListener = listener;
        addSendBuff(cmdAutoCal,Other);
        calOk = false;
        calInfo = "Error";
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
