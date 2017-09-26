package com.grean.dustctrl.protocol;

import android.os.StrictMode;
import android.util.Log;

import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.util.ArrayList;

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
    private float [] realTimeData = new float[7];
    //private static String realTimeDataBody = "&&";
    private long lastMinDate,minInterval,now;

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
       // Log.d(tag,string);
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
        //Log.d(tag,"copy data"+String.valueOf(data.getNoise()));
        realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
        realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
        realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
        realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
        realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
        realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
        realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();
    }

    private String getRealTimeDataString(long now){
        String timeString = tools.timeStamp2TcpString(now)+";";
        return  "QN="+timeString+"ST=32;CN=2011;PW=123456;MN="+mnCode+";CP=&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    private String getMinDataString(long now,long lastMinDate,long nexMinDate){
        return "QN="+tools.timeStamp2TcpString(now)+"ST=32;CN=2051;PW=123456;MN="+mnCode+";CP=&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
                insertMinData(getMeanData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate)))+"&&";
    }

    /**
     * 插入报警数据
     * @param data
     * @param alarmData
     * @return
     */
    private String insetRealTimeDataAlarmData(float[] data,float[] alarmData){
        String string;
        if(data[GeneralHistoryDataFormat.Dust] >= alarmData[GeneralHistoryDataFormat.Dust]){
            string = "01-Ala="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+",AlarmType =1";
        }else {
            string = "01-Ala=" + tools.float2String3(data[GeneralHistoryDataFormat.Dust]) + ",AlarmType =0";
        }
        return string;
    };

    private String insertSensorData (float[] data){
        //Log.d(tag,"paste data"+String.valueOf(data[GeneralHistoryDataFormat.Noise]));
        String string = "01-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+";01-Flag=3;";
        string += "Noise-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+";Noise-Flag=3;";
        string += "Humidity-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+";Humidity-Flag=3;";
        string += "Temperature-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+";Temperature-Flag=3;";
        string += "Pressure-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+";Pressure-Flag=3;";
        string += "WindSpeed-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+";WindSpeed-Flag=3;";
        string += "WindDirection-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+";WindDirection-Flag=3;";
        return string;
    }

    private String insertMinData(float[] data){
        String string = "01-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+";01-Flag=3;";//=2
        string += "Noise-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+";Noise-Flag=3;";
        string += "Humidity-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+";Humidity-Flag=3;";
        string += "Temperature-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+";Temperature-Flag=3;";
        string += "Pressure-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+";Pressure-Flag=3;";
        string += "WindSpeed-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+";WindSpeed-Flag=3;";
        string += "WindDirection-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+";WindDirection-Flag=3;";
        return string;
    }

    /**
     * 获取均值
     * @param format
     * @return
     */
    private float[] getMeanData(GeneralHistoryDataFormat format){
        float [] result = {0f,0f,0f,0f,0f,0f,0f};
        int size = format.getSize();
        for(int i=0;i<size;i++){
            ArrayList<Float> item = format.getItem(i);
            for(int j=0;j<7;j++){
                result[j] += item.get(j) / size;
            }
        }
        return result;
    }

    /**
     * 装帧头 数据长度 CRC 帧尾
     * @param body
     * @return
     */
    private String insertOneFrame(String body){
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
            GeneralDataBaseProtocol dataBaseProtocol = GetProtocols.getInstance().getDataBaseProtocol();
            dataBaseProtocol.loadMinDate();
            lastMinDate = dataBaseProtocol.getLastMinDate();
            Log.d(tag,"lastMinDate"+tools.timestamp2string(lastMinDate));
            while (run&&!interrupted()) {
                now = tools.nowtime2timestamp();
                addSendBuff(insertOneFrame(getRealTimeDataString(now)));
                if(now > lastMinDate){//发送分钟数据
                    //String string = insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate()));
                    //Log.d(tag,"send min data="+string);
                   // addSendBuff(string);
                    addSendBuff(insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate())));
                    lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
