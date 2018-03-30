package com.grean.dustctrl.protocol;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.grean.dustctrl.process.ScanSensor;
import com.grean.dustctrl.process.SensorData;
import com.tools;

import java.util.ArrayList;

/**
 * Created by weifeng on 2017/9/28.
 */

public class TcpClientHjt212 implements GeneralClientProtocol{
    private float [] realTimeData = new float[7];
    private static final String tag = "TcpClientHjt212";
    private TcpClientCallBack callBack;
    private boolean run;
    private String mnCode = "88888";
    private long lastMinDate,now;
    private HeartThread thread;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ClientDataBaseCtrl.UPDATE_REAL_TIME:
                    if(msg.obj!=null){
                        SensorData data = (SensorData) msg.obj;
                        realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
                        realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
                        realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
                        realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
                        realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
                        realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
                        realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();
                    }

                    break;
                default:

                    break;
            }
        }
    };

    public TcpClientHjt212(TcpClientCallBack callBack){
        this.callBack = callBack;
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
        realTimeData[GeneralHistoryDataFormat.Dust] = data.getDust();
        realTimeData[GeneralHistoryDataFormat.Temperature] = data.getAirTemperature();
        realTimeData[GeneralHistoryDataFormat.Humidity] = data.getAirHumidity();
        realTimeData[GeneralHistoryDataFormat.Pressure] = data.getAirPressure();
        realTimeData[GeneralHistoryDataFormat.Noise] = data.getNoise();
        realTimeData[GeneralHistoryDataFormat.WindDirection] = data.getWindDirection();
        realTimeData[GeneralHistoryDataFormat.WindForce] = data.getWindForce();
    }

    @Override
    public void setRealTimeAlarm(int alarm) {

    }

    private String getRealTimeDataString(long now){
        String timeString = tools.timeStamp2TcpStringWithoutMs(now)+";";
        return  "QN="+timeString+";ST=51;CN=2011;PW=123456;MN="+mnCode+";CP=&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
        //return  "ST=31;CN=2011;PW=123456;MN="+mnCode+";CP=&&DataTime="+timeString+insertSensorData(realTimeData)+"&&";
    }

    private String getMinDataString(long now,long lastMinDate,long nexMinDate){
        return "QN="+tools.timeStamp2TcpString(now)+";ST=51;CN=2051;PW=123456;MN="+mnCode+";CP=&DataTime="+tools.timeStamp2TcpString(lastMinDate)+";"+
               insertMinData(getMeanData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate)))+"&&";
        /*return "ST=31;CN=2061;PW=123456;MN="+mnCode+";CP=&&DataTime="+tools.timeStamp2TcpStringWithoutMs(lastMinDate)+";"+
                insertMinData(getMeanData(GetProtocols.getInstance().getDataBaseProtocol().getData(lastMinDate ,nexMinDate)))+"&&";*/
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
        /*string += "104-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+";104-Flag=N;";
        string += "105-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+";105-Flag=N;";
        string += "106-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+";106-Flag=N;";
        string += "107-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+";107-Flag=N;";
        string += "108-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+";108-Flag=N;";
        string += "109-Rtd="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+";109-Flag=N;";*/
        return string;
    }

    private String insertMinData(float[] data){
        String string = "103-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Dust])+";103-Flag=3;";//=2
        /*string += "104-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Noise])+";104-Flag=N;";
        string += "105-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Humidity])+";105-Flag=N;";
        string += "106-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Temperature])+";106-Flag=N;";
        string += "107-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.Pressure])+";107-Flag=N;";
        string += "108-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.WindForce])+";108-Flag=N;";
        string += "109-Cou="+tools.float2String3(data[GeneralHistoryDataFormat.WindDirection])+";109-Flag=N;";*/
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
            lastMinDate = dataBaseProtocol.getNextMinDate();
            Log.d(tag,"lastMinDate"+tools.timestamp2string(lastMinDate));
            ClientDataBaseCtrl dataBaseCtrl = ScanSensor.getInstance();
            while (run&&!interrupted()) {
                now = tools.nowtime2timestamp();
                /*String rtdString = insertOneFrame(getRealTimeDataString(now));
                Log.d(tag,"send rtd data="+rtdString);
                addSendBuff(rtdString);*/
                dataBaseCtrl.getRealTimeData(handler);
                addSendBuff(insertOneFrame(getRealTimeDataString(now)));
                if(now > lastMinDate){//发送分钟数据
                    dataBaseCtrl.saveMinData(now);
                    Log.d(tag,"now"+tools.timestamp2string(now)+"last"+tools.timestamp2string(dataBaseProtocol.getLastMinDate())+"next"+tools.timestamp2string(dataBaseProtocol.getNextMinDate()));
                    String string = insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate()));
                    Log.d(tag,"send min data="+string);
                    addSendBuff(string);
                    // addSendBuff(insertOneFrame(getMinDataString(now,dataBaseProtocol.getLastMinDate(),dataBaseProtocol.getNextMinDate())));
                    lastMinDate = dataBaseProtocol.calcNextMinDate(now);
                }
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
