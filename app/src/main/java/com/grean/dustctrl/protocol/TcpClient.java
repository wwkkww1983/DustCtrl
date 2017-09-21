package com.grean.dustctrl.protocol;

import android.os.StrictMode;
import android.util.Log;

import com.grean.dustctrl.process.SensorData;
import com.tools;

/**
 * Created by Administrator on 2017/9/1.
 */

public class TcpClient implements GeneralClientProtocol{
    private static final String tag = "TcpClient";
   // private String heartString = "Grean.com.cn";
    private TcpClientCallBack callBack;
    private String mnCode = "88888";
    private boolean run;
    private HeartThread thread;
    //private SensorData data;
    private String realTimeDataBody = "&&";

    public TcpClient(TcpClientCallBack callBack){
       this.callBack =  callBack;
    }
    @Override
    public void handleProtocol(byte[] rec, int count) {
        Log.d(tag,new String(rec,0,count));

    }

    @Override
    public void setMnCode(String string) {
        this.mnCode = string;
    }

    @Override
    public boolean addSendBuff(String string) {
        return callBack.addOneFrame(string.getBytes());
    }

    @Override
    public void startHeartBeatPacket() {
        thread = new HeartThread();
        thread.start();
    }

    @Override
    public void stopHeartBeatPacket() {
        run = false;
    }

    @Override
    public void setRealTimeData(SensorData data) {
        //this.data = data;
        long now = tools.nowtime2timestamp();
        String timeString = tools.timeStamp2TcpString(now)+";";
        realTimeDataBody = "QN="+timeString+"ST=21;CN=9011;PW=123456;MN="+mnCode+";CP=&DateTime="+timeString+insertSensorData(data)+"&&";
    }

    private String insertSensorData (SensorData data){
        String string = "Dust-Rtd"+tools.float2String3(data.getDust())+";Dust-Flag=N;";
        string += "Noise-Rtd"+tools.float2String3(data.getNoise())+";Noise-Flag=N;";
        string += "Humidity-Rtd"+tools.float2String3(data.getAirHumidity())+";Humidity-Flag=N;";
        string += "Temperature-Rtd"+tools.float2String3(data.getAirTemperature())+";Temperature-Flag=N;";
        string += "Pressure-Rtd"+tools.float2String3(data.getAirPressure())+";Pressure-Flag=N;";
        string += "WindSpeed-Rtd"+tools.float2String3(data.getWindForce())+";WindSpeed-Flag=N;";
        string += "WindDirection-Rtd"+tools.float2String3(data.getWindDirection())+";WindDirection-Flag=N;";
        return string;

    }

    /**
     * 装帧头 数据长度 CRC 帧尾
     * @param body
     * @return
     */
    synchronized private String insertOneFrame(String body){
        byte [] bodyBuff = body.getBytes();
        int crc = tools.calcCrc16(bodyBuff);
        byte [] crcBuff = tools.int2bytes(crc);
        byte [] crcFormatBuff = new byte[2];
        crcFormatBuff[0] = crcBuff[2];
        crcFormatBuff[1] = crcBuff[3];
        String crcString = tools.bytesToHexString(crcFormatBuff,crcFormatBuff.length);
        String lenString = String.format("%04d",bodyBuff.length);
        return "##"+lenString+body+crcString+"\r\n";
    }

    private class HeartThread extends Thread{

        @Override
        public void run() {
            run = true;
            while (run&&!interrupted()) {
                addSendBuff(insertOneFrame(realTimeDataBody));
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
